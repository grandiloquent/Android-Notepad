#include <jni.h>
#include "sqlite3.h"
#include "utils.h"
#include "mongoose.h"
#include <net/if.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include "defines.h"
#include "strbuf.h"
#include "cJSON.h"

static struct mg_serve_http_opts s_http_server_opts;
static sqlite3 *s_db;
static char s_device[512];


sqlite3 *CreateDatabase(const char *fileName);

static void ev_handler(struct mg_connection *nc, int ev, void *ev_data);

void GetAssetFile(JNIEnv *ev, jobject assetManager);

void GetIP(char *device);

static void GetJSON(struct mg_connection *nc, const struct http_message *hm, int id);

void GetPath(JNIEnv *env, char *buf);

static int has_prefix(const struct mg_str *uri, const struct mg_str *prefix);

static void index(struct mg_connection *nc, const struct http_message *hm);

static int is_equal(const struct mg_str *s1, const struct mg_str *s2);

void StartServer(const char *address);

static void UpdateJSON(struct mg_connection *nc, const struct http_message *hm);


sqlite3 *CreateDatabase(const char *fileName) {
    sqlite3 *db;
    int rc = sqlite3_open(fileName, &db);
    if (rc) {
        LOGE("Can't open database: %s\n", sqlite3_errmsg(db));
        return NULL;
    }
    const char *sql = "CREATE TABLE IF NOT EXISTS note (\n" \
                      "    _id INTEGER PRIMARY KEY AUTOINCREMENT,\n" \
                      "    title VARCHAR(50) NOT NULL,\n" \
                      "    content TEXT NOT NULL,\n" \
                      "    created_at BIGINT NOT NULL,\n" \
                      "    updated_at BIGINT NOT NULL,\n" \
                      "    UNIQUE (created_at)\n" \
                      ");";
    char *errMsg;
    rc = sqlite3_exec(db, sql, NULL, 0, &errMsg);
    if (rc != SQLITE_OK) {
        LOGE("SQL error:%s\n", errMsg);
        sqlite3_free(errMsg);
    }
    return db;
}

static void ev_handler(struct mg_connection *nc, int ev, void *ev_data) {
    static const struct mg_str api_index = MG_MK_STR("/");
    static const struct mg_str api_get = MG_MK_STR("/api/get/");
    static const struct mg_str api_update = MG_MK_STR("/api/update");
    struct http_message *hm = (struct http_message *) ev_data;
    if (ev == MG_EV_HTTP_REQUEST) {
        if (is_equal(&hm->uri, &api_index)) {
            index(nc, hm);
        } else if (has_prefix(&hm->uri, &api_get)) {
            GetJSON(nc, hm, atoi(hm->uri.p + api_get.len));
        } else if (is_equal(&hm->uri, &api_update)) {
            UpdateJSON(nc, hm);
        } else {
            LOGE("ev_handler: %s\n", s_http_server_opts.document_root);
            mg_serve_http(nc, hm, s_http_server_opts); /* Serve static content */
        }
    }
}

void GetAssetFile(JNIEnv *ev, jobject assetManager) {
    AAssetManager *manager = AAssetManager_fromJava(ev, assetManager);
    if (manager == NULL) {
        return;
    }
}

void GetIP(char *device) {
    int i = 0;
    int sockfd;
    struct ifconf ifconf;
    unsigned char buf[512];
    struct ifreq *ifreq;
    //初始化ifconf
    ifconf.ifc_len = 512;
    ifconf.ifc_buf = (char *) buf;
    if ((sockfd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        return;
    }
    ioctl(sockfd, SIOCGIFCONF, &ifconf); //获取所有接口信息
    //接下来一个一个的获取IP地址
    ifreq = (struct ifreq *) buf;
    for (i = (ifconf.ifc_len / sizeof(struct ifreq)); i > 0; i--) {
        // if(ifreq->ifr_flags == AF_INET){ //for ipv4
        char *ip = inet_ntoa(((struct sockaddr_in *) &(ifreq->ifr_addr))->sin_addr);
        // LOGE("ip: %s\n", ip);
        if (strncmp(ip, "192.168", 7) == 0)
            strcpy(device, ip);
        // __android_log_print(ANDROID_LOG_INFO, "test", "%s", ip);
        ifreq++;
        // }
    }
}

static void GetJSON(struct mg_connection *nc, const struct http_message *hm, int id) {
    sqlite3_stmt *stmt = NULL;
    const char *data = NULL;
    int result = SQLITE_BUSY;
    const char *sql = "select title,content from note where _id=?";
    (void) hm;
    if (sqlite3_prepare_v2(s_db, sql, strlen(sql), &stmt,
                           NULL) == SQLITE_OK) {
        sqlite3_bind_int(stmt, 1, id);
        struct cJSON *json = cJSON_CreateObject();
        for (int i = 0; i < 10; i++) {
            result = sqlite3_step(stmt);
            if (result != SQLITE_BUSY && result != SQLITE_LOCKED) {
                break;
            }
            usleep(200);
        }
        if (result == SQLITE_DONE || result == SQLITE_ROW) {
            data = (char *) sqlite3_column_text(stmt, 0);
            cJSON_AddItemToObject(json, "title", cJSON_CreateString(data));
            data = (char *) sqlite3_column_text(stmt, 1);
            cJSON_AddItemToObject(json, "content", cJSON_CreateString(data));
        }
        char *buf = cJSON_Print(json);
        sqlite3_finalize(stmt);
        cJSON_Delete(json);
        //LOGE("%s\n",buf );
        mg_send_head(nc, 200, strlen(buf),
                     "Content-Type: application/json; charset=utf-8");
        mg_send(nc, buf, (int) strlen(buf));
    } else {
        mg_printf(nc, "%s",
                  "HTTP/1.1 500 Server Error\r\n"
                  "Content-Length: 0\r\n\r\n");
    }
}

void GetPath(JNIEnv *env, char *buf) {
    jclass envcls = (*env)->FindClass(env, "android/os/Environment"); //获得类引用
    if (envcls == NULL) return;
    //找到对应的类，该类是静态的返回值是File
    jmethodID id = (*env)->GetStaticMethodID(env, envcls, "getExternalStorageDirectory",
                                             "()Ljava/io/File;");
    //调用上述id获得的方法，返回对象即File file=Enviroment.getExternalStorageDirectory()
    //其实就是通过Enviroment调用 getExternalStorageDirectory()
    jobject fileObj = (*env)->CallStaticObjectMethod(env, envcls, id, "");
    //通过上述方法返回的对象创建一个引用即File对象
    jclass flieClass = (*env)->GetObjectClass(env, fileObj); //或得类引用
    //在调用File对象的getPath()方法获取该方法的ID，返回值为String 参数为空
    jmethodID getpathId = (*env)->GetMethodID(env, flieClass, "getPath", "()Ljava/lang/String;");
    //调用该方法及最终获得存储卡的根目录
    jstring pathStr = (jstring) (*env)->CallObjectMethod(env, fileObj, getpathId, "");
    char *path = (*env)->GetStringUTFChars(env, pathStr, NULL);
    //CreateDatabase(fileName);
    strcpy(buf, path);
    (*env)->ReleaseStringUTFChars(env, pathStr, path);
}

static int has_prefix(const struct mg_str *uri, const struct mg_str *prefix) {
    return uri->len > prefix->len && memcmp(uri->p, prefix->p, prefix->len) == 0;
}

static void index(struct mg_connection *nc, const struct http_message *hm) {
    sqlite3_stmt *stmt = NULL;
    const char *data = NULL;
    int result;
    (void) hm;
    if (sqlite3_prepare_v2(s_db, "select _id,title from note order by updated_at desc", -1, &stmt,
                           NULL) == SQLITE_OK) {
        struct strbuf name = STRBUF_INIT;
        while (1) {
            result = sqlite3_step(stmt);
            if (result == SQLITE_ROW) {
                data = (char *) sqlite3_column_text(stmt, 1);
                strbuf_addf(
                        &name,
                        "<li class=\"nav-tree__item\"><a class=\"nav-tree__link\" href=\"#%d\">%s</a></li>",
                        sqlite3_column_int(stmt, 0), data);
            } else {
                break;
            }
        }
        sqlite3_finalize(stmt);
        mg_send_head(nc, 200, S_INDEX_T1 + S_INDEX_T2 + name.len,
                     "Content-Type: text/html; charset=utf-8");
        mg_send(nc, s_index_t1, S_INDEX_T1);
        mg_send(nc, name.buf, (int) name.len);
        strbuf_release(&name);
        mg_send(nc, s_index_t2, S_INDEX_T2);
    } else {
        mg_printf(nc, "%s",
                  "HTTP/1.1 500 Server Error\r\n"
                  "Content-Length: 0\r\n\r\n");
    }
}

static int is_equal(const struct mg_str *s1, const struct mg_str *s2) {
    return s1->len == s2->len && memcmp(s1->p, s2->p, s2->len) == 0;
}

void StartServer(const char *address) {
    struct mg_mgr mgr;
    struct mg_connection *nc;
    int i;
    mg_mgr_init(&mgr, NULL);
    nc = mg_bind(&mgr, address, ev_handler);
    mg_set_protocol_http_websocket(nc);
    for (;;) {
        mg_mgr_poll(&mgr, 1000);
    }
    mg_mgr_free(&mgr);
}

static void UpdateJSON(struct mg_connection *nc, const struct http_message *hm) {
//    int i;
//
//    for (i = 0; i < MG_MAX_HTTP_HEADERS && hm->header_names[i].len > 0; i++) {
//        struct mg_str hn = hm->header_names[i];
//        struct mg_str hv = hm->header_values[i];
//        LOGE("%s\"%.*s\": \"%.*s\"", (i != 0 ? "," : ""), (int) hn.len,
//             hn.p, (int) hv.len, hv.p);
//    }


    cJSON *json = cJSON_Parse(hm->body.p);
    cJSON *jsTitle = cJSON_GetObjectItem(json, "title");
    cJSON *jsId = cJSON_GetObjectItem(json, "id");
    cJSON *jsContent = cJSON_GetObjectItem(json, "content");

    int id = jsId->valueint;
    char *title = jsTitle->valuestring;
    char *content = jsContent->valuestring;
//    LOGE("the body length: %d %d %d %s\n", strlen(hm->body.p), hm->body.len, strlen(content),
//         hm->body.p);


    sqlite3_stmt *stmt = NULL;
    int result = SQLITE_BUSY;
    const char *sql;
    if (id > 0) {
        sql = "Update note Set title=?,content=?,updated_at=? where _id=?";

    } else {
        //replace(strftime('%Y%m%d%H%M%f', 'now'), '.', '') datetime('now') datetime('now')
        sql = "INSERT INTO note VALUES(NULL,?,?,?,?)";
    }
    (void) hm;
    if (sqlite3_prepare_v2(s_db, sql, -1, &stmt,
                           NULL) == SQLITE_OK) {
        struct timeval tv;
        gettimeofday(&tv, NULL);

        double time_in_mill =
                (tv.tv_sec) * 1000 + (tv.tv_usec) / 1000;

        sqlite3_bind_text(stmt, 1, title, -1, SQLITE_STATIC);
        sqlite3_bind_text(stmt, 2, content, -1, SQLITE_STATIC);
        if (id > 0) {
            sqlite3_bind_double(stmt, 3, time_in_mill);
            sqlite3_bind_int(stmt, 4, id);

        } else {
            sqlite3_bind_double(stmt, 3, time_in_mill);
            sqlite3_bind_double(stmt, 4, time_in_mill);
        }

        struct cJSON *resJson = cJSON_CreateObject();
        for (int i = 0; i < 10; i++) {
            result = sqlite3_step(stmt);
            if (result != SQLITE_BUSY && result != SQLITE_LOCKED) {
                break;
            }
            usleep(200);
        }
        if (result == SQLITE_DONE || result == SQLITE_ROW) {


            cJSON_AddItemToObject(resJson, "title", cJSON_CreateString(title));

        }
        char *buf = cJSON_Print(resJson);
        sqlite3_finalize(stmt);
        cJSON_Delete(resJson);
        cJSON_Delete(json);
        //LOGE("%s\n",buf );
        mg_send_head(nc, 200, strlen(buf),
                     "Content-Type: application/json; charset=utf-8");
        mg_send(nc, buf, (int) strlen(buf));


    } else {
        mg_printf(nc, "%s",
                  "HTTP/1.1 500 Server Error\r\n"
                  "Content-Length: 0\r\n\r\n");
    }
}

void *start_server(void *ignore) {

    StartServer(s_device);
    return NULL;
}

JNIEXPORT jstring JNICALL
Java_euphoria_psycho_notepad_NativeMethods_startServer(JNIEnv *env, jclass type, jstring fileName_,
                                                       jstring staticDirectory_) {
    const char *fileName = (*env)->GetStringUTFChars(env, fileName_, 0);
    const char *staticDirectory = (*env)->GetStringUTFChars(env, staticDirectory_, 0);


    s_db = CreateDatabase(fileName);


    GetIP(s_device);
    strcat(s_device, ":8090");

    char *dir = malloc(strlen(staticDirectory) + 1);
    strcpy(dir, staticDirectory);
    s_http_server_opts.document_root = dir;


    pthread_t t;
//    char device[512]; //should pass malloc(512)
//    strcpy(device, "123");
//    strcat(device, "456");
//    LOGE("%p %s\n", (void *) device, (char *) device);
//    //Launch a thread
//    pthread_create(&t, NULL, start_server, (void *) device);

    pthread_create(&t, NULL, start_server, NULL);
    (*env)->ReleaseStringUTFChars(env, fileName_, fileName);
    (*env)->ReleaseStringUTFChars(env, staticDirectory_, staticDirectory);

    return (*env)->NewStringUTF(env, s_device);
}