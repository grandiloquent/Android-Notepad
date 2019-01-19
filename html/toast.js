;
(function () {

    var Toast = function Toast(element) {
        this.element = element;
        this.init();
    }

    Toast.prototype.onHidden = function () {
        if (!this.element.classList.contains(this.Constants.Hidden))
            this.element.classList.add(this.Constants.Hidden);
    }
    Toast.prototype.Constants = {
        Hidden: "toast--hidden",
        Close: ".toast__close",
        HiddenDelay: 5000
    }

    Toast.prototype.init = function () {
        if (!this.element) return;
        this.close = this.element.querySelector(this.Constants.Close);
        if (this.close) {
            this.close.addEventListener('click', () => {});
        }

        setTimeout(() => this.onHidden(), this.Constants.HiddenDelay);
    }
    window['Toast'] = Toast;

    new Toast(document.querySelector('.toast__container'));
})()