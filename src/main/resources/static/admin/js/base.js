$.extend({
    Blog: function () {
    },
});

/**
 * 提取jwt token中的payload以对象的形式返回
 * @param str
 * @returns {payload}
 */
function jwtDecode(token) {
    var payload = token.split(".")[1];
    var result = base64UrlDecode(payload);
    return JSON.parse(result);
}

/**
 * 解码base64
 * @param str
 * @returns {string}
 */
function base64UrlDecode(str) {
    str = str.replace(/-/g, '+'); // 62nd char of encoding
    str = str.replace(/_/g, '/'); // 63rd char of encoding
    switch (str.length % 4) // Pad with trailing '='s
    {
        case 0:
            break; // No pad chars in this case
        case 2:
            str += "==";
            break; // Two pad chars
        case 3:
            str += "=";
            break; // One pad char
        default:
            throw new Error("Illegal base64url string!");
    }
    return decodeURIComponent(window.escape(window.atob(str))); // Standard base64 decoder
}

/**
 * 成功弹框
 * @param options
 */
$.Blog.prototype.alertOk = function (options) {
    options = options.length ? {text: options} : (options || {});
    options.title = options.title || '操作成功';
    options.text = options.text;
    options.showCancelButton = false;
    options.showCloseButton = false;
    options.type = 'success';
    this.alertBox(options);
};

/**
 * 弹出成功，并在500毫秒后刷新页面
 * @param text
 */
$.Blog.prototype.alertOkAndReload = function (text) {
    this.alertOk({
        text: text, then: function () {
            setTimeout(function () {
                window.location.reload();
            }, 700);
        }
    });
};

/**
 * 警告弹框
 * @param options
 */
$.Blog.prototype.alertWarn = function (options) {
    options = options.length ? {text: options} : (options || {});
    options.title = options.title || '警告信息';
    options.text = options.text;
    options.timer = 3000;
    options.type = 'warning';
    this.alertBox(options);
};

/**
 * 询问确认弹框，这里会传入then函数进来
 * @param options
 */
$.Blog.prototype.alertConfirm = function (options) {
    options = options || {};
    options.title = options.title || '确定要删除吗？';
    options.text = options.text;
    options.showCancelButton = true;
    options.type = 'question';
    this.alertBox(options);
};

/**
 * 错误提示
 * @param options
 */
$.Blog.prototype.alertError = function (options) {
    options = options.length ? {text: options} : (options || {});
    options.title = options.title || '错误信息';
    options.text = options.text;
    options.type = 'error';
    this.alertBox(options);
};

/**
 * 公共弹框
 * @param options
 */
$.Blog.prototype.alertBox = function (options) {
    swal({
        title: options.title,
        text: options.text,
        type: options.type,
        timer: options.timer || 9999,
        showCloseButton: options.showCloseButton,
        showCancelButton: options.showCancelButton,
        showLoaderOnConfirm: options.showLoaderOnConfirm || false,
        confirmButtonColor: options.confirmButtonColor || '#3085d6',
        cancelButtonColor: options.cancelButtonColor || '#d33',
        confirmButtonText: options.confirmButtonText || '确定',
        cancelButtonText: options.cancelButtonText || '取消'
    }).then(function (e) {
        options.then && options.then(e);
    }).catch(swal.noop);
};

function getCookieValue(name) {
    var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
    if (arr = document.cookie.match(reg)) {
        return decodeURIComponent(arr[2]);
    }
    return "";
}

window.axios.defaults.headers.common = {
    'X-Requested-With': 'XMLHttpRequest'
};

$.Blog.prototype.get = function (options) {
    axios.get(options.url, {
        params: options.data || {}
    }).then(function (response) {
        options.success && options.success(response.data)
    }).catch(function (error) {
        options.error && options.error(error)
    });
};

$.Blog.prototype.put = function (options) {
    var self = this;
    axios.put(options.url, options.data || {}).then(function (response) {
        self.hideLoading();
        options.success && options.success(response.data);
    }).catch(function (error) {
        options.error && options.error(error)
    });
};

/**
 * 全局post函数
 *
 * @param options   参数
 */
$.Blog.prototype.post = function (options) {
    var self = this;
    axios.post(options.url, options.data || {}).then(function (response) {
        self.hideLoading();
        options.success && options.success(response.data);
    }).catch(function (error) {
        options.error && options.error(error)
    });
};

/**
 * 全局patch函数
 *
 * @param options   参数
 */
$.Blog.prototype.patch = function (options) {
    var self = this;
    axios.patch(options.url, options.data || {}).then(function (response) {
        self.hideLoading();
        options.success && options.success(response.data);
    }).catch(function (error) {
        options.error && options.error(error)
    });
};

/**
 * 全局delete函数
 *
 * @param options   参数
 */
$.Blog.prototype.delete = function (options) {
    var self = this;
    axios.delete(options.url, options.data || {}).then(function (response) {
        self.hideLoading();
        options.success && options.success(response.data);
    }).catch(function (error) {
        options.error && options.error(error)
    });
};

/**
 * 通过 FORM 表单方式提交
 * @param options
 */
$.Blog.prototype.postWithForm = function (options) {
    var self = this;
    axios({
        url: options.url,
        method: 'post',
        data: options.data || {},
        transformRequest: [function (data) {
            // Do whatever you want to transform the data
            var ret = [];
            for (let it in data) {
                ret.push(encodeURIComponent(it) + '=' + encodeURIComponent(data[it]) + '&');
            }
            return ret.join('');
        }],
        headers: {
            'Authorization': "",
            'X-Requested-With': 'XMLHttpRequest',
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    }).then(function (response) {
        self.hideLoading();
        options.success && options.success(response.data);
    }).catch(function (error) {
        options.error && options.error(error)
    });
};

/**
 * 显示动画
 */
$.Blog.prototype.showLoading = function () {
    if ($('#blog-loading').length === 0) {
        $('body').append('<div id="blog-loading"></div>');
    }
    $('#blog-loading').show();
};

/**
 * 隐藏动画
 */
$.Blog.prototype.hideLoading = function () {
    $('#blog-loading') && $('#blog-loading').hide();
};

$.Blog.prototype.copy = function (src) {
    var dst = {};
    for (var prop in src) {
        if (src.hasOwnProperty(prop)) {
            dst[prop] = src[prop];
        }
    }
    return dst;
};
$.Blog.prototype.getUrlParam = function(name) {
    // 构造一个含有目标参数的正则表达式对象
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    // 匹配目标参数window.location.search方法是截取当前url中“?”后面的字符串
    var r = window.location.search.substr(1).match(reg);
    if (r != null) {
        return unescape(r[2]);
    } else {
        return null;
    }

}

/**
 * gravatar 头像地址生成
 */
$.Blog.prototype.gravatar = function (email, options) {
    //check to make sure you gave us something
    var options = options || {},
        base,
        params = [];

    //set some defaults, just in case
    options = {
        size: options.size || "50",
        rating: options.rating || "g",
        secure: options.secure || (location.protocol === 'https:'),
        backup: options.backup || ""
    };

    //setup the email address
    email = email.trim().toLowerCase();

    //determine which base to use
    base = options.secure ? 'https://cn.gravatar.com/avatar/' : 'http://cn.gravatar.com/avatar/';

    //add the params
    if (options.rating) {
        params.push("r=" + options.rating)
    }
    ;
    if (options.backup) {
        params.push("d=" + encodeURIComponent(options.backup))
    }
    ;
    if (options.size) {
        params.push("s=" + options.size)
    }
    ;

    //now throw it all together
    return base + md5(email) + "?" + params.join("&");
};

/**
 * Vue 全局
 */
Vue.filter('formatDate', function (value, pattern) {
    if (value) {
        return moment(value).format(pattern || 'YYYY-MM-DD HH:mm:ss')
    }
    return ''
});
Vue.filter('truncate', function (value, size, append) {
    if (value && value.length >= size) {
        return value.substring(0, size) + (append || '...');
    }
    return value
});
Vue.filter('gravatar', function (value) {
    return new $.Blog().gravatar(value)
});

Vue.use(VueLoading);
Vue.component('Loading', VueLoading)
var vueLoding;

var colors_ = ["default", "primary", "success", "info", "warning", "danger", "inverse", "purple", "pink"];
Vue.prototype.randomColor = function () {
    return colors_[Math.floor(Math.random() * colors_.length)];
};
