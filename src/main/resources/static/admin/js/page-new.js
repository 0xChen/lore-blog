let mditor, htmlEditor;
const blog = new $.Blog();
// 每60秒自动保存一次草稿
let refreshIntervalId;
Dropzone.autoDiscover = false;
Vue.component('v-select', VueSelect.VueSelect);
const vm = new Vue({
    el: '#app',
    data: {
        post: {
            id: '',
            title: '',
            name: '',
            thumbnail: '',
            content: '',
            status: '1',// 草稿
            type: 'page',// 文章
            contentType: 'markdown',// markdown
            commentStatus: '1',// 允许评论
            pingStatus: '0',
            pubdate: null,
            categoryId: 0
        },
        isLoading: true
    },
    beforeCreate: function () {
        vueLoding = this.$loading.show();
    },
    mounted: function () {
        const $vm = this;
        $vm.load();
        refreshIntervalId = setInterval("vm.autoSave()", 10 * 1000);
    },
    methods: {
        load: function () {

        },
        autoSave: function (callback) {
            let $vm = this;
            let content = $vm.post.contentType === 'markdown' ? mditor.value : htmlEditor.summernote('code');
            if ($vm.post.title !== '' && content !== '') {
                $vm.post.content = content;
                let post = blog.copy($vm.post);
                if (post.status === "2") {
                    post.status = '1';
                }
                let requestMethod = $vm.post.id ? "put" : "post";
                blog[requestMethod]({
                    url: '/admin/api/page',
                    data: post,
                    success: function (result) {
                        if (result && result.success) {
                            $vm.post.id = result.data.id;
                            callback && callback();
                        } else {
                            blog.alertError(result.message || '保存失败');
                        }
                    },
                    error: function (error) {
                        console.log(error);
                        clearInterval(refreshIntervalId);
                    }
                });
            }
        },
        switchEditor: function (event) {
            let contentType = this.post.contentType;
            let this_ = event.target;
            if (contentType === 'markdown') {
                // 切换为富文本编辑器
                if ($('#md-container .markdown-body').html().length > 0) {
                    $('#html-container .note-editable').empty().html($('#md-container .markdown-body').html());
                    $('#html-container .note-placeholder').hide();
                }
                mditor.value = '';
                $('#md-container').hide();
                $('#html-container').show();

                this_.innerHTML = '切换为Markdown编辑器';

                this.post.contentType = 'html';
            } else {
                // 切换为markdown编辑器
                if ($('#html-container .note-editable').html().length > 0) {
                    mditor.value = '';
                    mditor.value = toMarkdown($('#html-container .note-editable').html());
                }
                $('#html-container').hide();
                $('#md-container').show();

                this.post.contentType = 'markdown';

                this_.innerHTML = '切换为富文本编辑器';
                htmlEditor.summernote("code", "");
            }
        },
        save: function (status) {
            let $vm = this;
            let content = this.post.contentType === 'markdown' ? mditor.value : htmlEditor.summernote('code');
            let title = $vm.post.title;
            if (title === '') {
                blog.alertWarn('请输入标题');
                return;
            }
            if (content === '') {
                blog.alertWarn('请输入内容');
                return;
            }
            clearInterval(refreshIntervalId);
            $vm.post.status = status;
            $vm.autoSave(function () {
                blog.alertOk({
                    text: '发布成功',
                    then: function () {
                        setTimeout(function () {
                            window.location.href = '/admin/pages';
                        }, 500);
                    }
                });
            });
        }
    }
});

$(document).ready(function () {
    mditor = window.mditor = Mditor.fromTextarea(document.getElementById('md-editor'));
    // 富文本编辑器
    htmlEditor = $('.summernote').summernote({
        lang: 'zh-CN',
        height: 340,
        placeholder: '写点儿什么吧...',
        //上传图片的接口
        callbacks: {
            onImageUpload: function (files) {
                let data = new FormData();
                data.append('file', files[0]);
                blog.showLoading();
                $.ajax({
                    url: '/file',
                    method: 'POST',
                    data: data,
                    processData: false,
                    dataType: 'json',
                    headers: {},
                    contentType: false,
                    success: function (result) {
                        blog.hideLoading();
                        if (result && result.success) {
                            let url = result.data;
                            console.log('url =>' + url);
                            htmlEditor.summernote('insertImage', url);
                        } else {
                            blog.alertError(result.message || '图片上传失败');
                        }
                    }
                });
            }
        }
    });

    $('#commentStatus').bootstrapToggle({
        on: '开启',
        off: '关闭'
    });

    $('#addThumbnail').bootstrapToggle({
        on: '添加',
        off: '取消'
    });

    $('#commentStatus').change(function () {
        var checked = $(this).prop('checked');
        if (checked) {
            vm.post.commentStatus = '1';
        } else {
            vm.post.commentStatus = '0';
        }
    });


    $('#addThumbnail').change(function () {
        var checked = $(this).prop('checked');
        if (checked) {
            $('#dropzone-container').removeClass('hide');
            $('#dropzone-container').show();
            var thumbImage = $("#dropzone").css("backgroundImage");
            if (thumbImage && thumbImage.indexOf('url') !== -1) {
                thumbImage = thumbImage.split("(")[1].split(")")[0];
                vm.post.thumbnail = thumbImage.substring(1, thumbImage.length - 1);
            }
        } else {
            $('#dropzone-container').addClass('hide');
            vm.post.thumbnail = '';
        }
    });

    var thumbdropzone = $('.dropzone');

    // 缩略图上传
    $("#dropzone").dropzone({
        url: "/file",
        filesizeBase: 1024,//定义字节算法 默认1000
        maxFilesize: '15', //MB
        acceptedFiles: 'image/*',
        dictFileTooBig: '您的文件超过15MB!',
        dictInvalidInputType: '不支持您上传的类型',
        headers: {},
        fallback: function () {
            blog.alertError('暂不支持您的浏览器上传!');
        },
        init: function () {
            this.on('success', function (files, result) {
                console.log("upload success..");
                console.log(" result => " + result);
                if (result && result.success) {
                    var url = '/file/' + result.data;
                    console.log('url => ' + url);

                    vm.post.thumbnail = url;
                    thumbdropzone.css('background-image', 'url(' + url + ')');
                    thumbdropzone.css('background-size', 'cover');
                    $('.dz-preview').hide();
                }
            });
            this.on('error', function (a, errorMessage, result) {
                if (errorMessage && !result) {
                    blog.alertError(errorMessage);
                    return;
                }
                if (!result.success && result.message) {
                    blog.alertError(result.message || '缩略图上传失败');
                }
            });
        }
    });

    vm.isLoading = false;
    vueLoding.hide();
});