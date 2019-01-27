

#ifndef NOTEPAD_UTILS_H
#define NOTEPAD_UTILS_H

#include <android/log.h>

#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, "TAG::", __VA_ARGS__))
#define LOGE(...) \
  ((void)__android_log_print(ANDROID_LOG_ERROR, "TAG::", __VA_ARGS__))


#endif //NOTEPAD_UTILS_H
