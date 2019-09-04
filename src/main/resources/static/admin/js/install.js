(function ($) {

    "use strict";
    var blog = new $.Blog();
    var FormWizard = function () {
    };
    //creates form with validation
    FormWizard.prototype.init = function () {
        var $form_container = $("#wizard-validation-form");
        $form_container.validate({
            errorPlacement: function errorPlacement(error, element) {
                element.after(error);
            }
        });
        $form_container.children("div").steps({
            headerTag: "h3",
            bodyTag: "section",
            transitionEffect: "slideLeft",
            labels: {
                previous: "上一步",
                next: "下一步",
                finish: "登录后台",
                loading: '加载中...',
                current: '当前位置'
            },
            onStepChanging: function (event, currentIndex, newIndex) {
                blog.showLoading();
                $form_container.validate().settings.ignore = ":disabled,:hidden";
                if (currentIndex > newIndex) {
                    // 上一步
                    return true;
                }
                var isValid = $form_container.valid();
                if (!isValid) {
                    blog.hideLoading();
                }
                if (isValid) {
                    isValid = false;
                    blog.showLoading();
                    let postUrl;
                    let postData;
                    if (currentIndex === 0) {
                        postUrl = constant.INSTALL_URI + "/option";
                        postData = {
                            "blog_title": $("#blog_title").val(),
                            "scheme": $("#scheme").val(),
                            "hostname": $("#hostname").val(),
                            "blog_description": $("#blog_description").val()
                        };
                    } else if (currentIndex === 1) {
                        postUrl = constant.INSTALL_URI + "/user";
                        postData = {
                            "nickname": $("#nickname").val(),
                            "username": $("#username").val(),
                            "password": $("#password").val(),
                            "email": $("#email").val()
                        };
                    } else {
                        return isValid;
                    }
                    $.ajax({
                        url: postUrl,
                        type: 'POST',
                        async: false,
                        data: postData,
                        dataType: 'json',
                        success: function (result) {
                            if (result && result.success) {
                                isValid = true;
                            } else {
                                blog.alertError(result.message || '安装失败');
                            }
                        },
                        error: function (e) {
                            console.log(e.responseJSON);
                        }
                    });
                    return isValid;
                } else {
                    return isValid;
                }

            },
            onStepChanged: function (event, currentIndex, priorIndex) {
                if (currentIndex === 2) {
                    $.ajax({
                        url: constant.INSTALL_URI + "/lock",
                        type: 'POST',
                        async: false,
                        data: {},
                        dataType: 'json',
                        success: function (result) {
                            if (!result.success) {
                                blog.alertError(result.message || '安装失败, 无法创建锁定文件');
                            }
                        },
                        error: function (e) {
                            console.log(e.responseJSON);
                            blog.alertError('安装失败, 无法创建锁定文件');
                        }
                    });
                }
                blog.hideLoading();
            },
            onFinishing: function (event, currentIndex) {
                $form_container.validate().settings.ignore = ":disabled";
                var isValid = $form_container.valid();
                return isValid;
            },
            onFinished: function (event, currentIndex) {
                window.location.href = "/admin/login";
            }
        });

        return $form_container;
    };
    //init
    $.FormWizard = new FormWizard;
    $.FormWizard.Constructor = FormWizard
})(window.jQuery);
$.FormWizard.init();
$("#hostname").val(document.location.host);
$("#scheme").val(document.location.protocol.replace(":", ""));
