let mditor, htmlEditor, converter = new showdown.Converter({tables: true, underline: true, tasklists: true});
converter.setFlavor('github');
const blog = new $.Blog();
// 每60秒自动保存一次草稿
var refreshIntervalId;
Dropzone.autoDiscover = false;

var vm = new Vue({
    el: '#app',
    data: {
        post: {
            id: '',
            title: '',
            name: '',
            thumbnail: '',
            content: '',
            tags: '',
            status: '1',// 草稿
            type: 'post',// 文章
            contentType: 'markdown',// markdown
            commentStatus: '1',// 允许评论
            pingStatus: '1',// 允许Ping
            pubdate: null,// 发布时间
            categoryId: 0
        },
        categories: [],
        isLoading: true
    },
    beforeCreate: function(){
        vueLoding = this.$loading.show();
    },
    mounted: function () {
        var $vm = this;
        $("#pubdate").datetimepicker({
            format: 'YYYY-MM-DD HH:mm:ss',
            locale: 'zh-cn'
        });
        mditor = window.mditor = Mditor.fromTextarea(document.getElementById('md-editor'));
        // 富文本编辑器
        htmlEditor = $('.summernote').summernote({
            lang: 'zh-CN',
            height: 340,
            placeholder: '写点儿什么吧...',
            //上传图片的接口
            callbacks: {
                onImageUpload: function (files) {
                    var data = new FormData();
                    data.append('file', files[0]);
                    blog.showLoading();
                    $.ajax({
                        url: '/file',
                        method: 'POST',
                        data: data,
                        processData: false,
                        dataType: 'json',
                        headers: {
                        },
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

        $vm.load();
        refreshIntervalId = setInterval("vm.autoSave()", 10 * 1000);
    },
    methods: {
        load: function () {
            var $vm = this;
            var pos = window.location.toString().lastIndexOf("/");
            var postId = window.location.toString().substring(pos + 1)
            blog.get({
                url: '/admin/api/categories',
                success: function (result) {
                    $vm.categories = result.data
                },
                error: function (error) {
                    console.log(error);
                    alert('数据加载失败');
                }
            });
            blog.get({
                url: '/admin/posts/' + postId,
                success: function (result) {
                    $vm.post = result.data;
                    if ($vm.post.pubdate) {
                        $vm.post.pubdate = moment($vm.post.pubdate).format('YYYY-MM-DD HH:mm:ss');
                    }

                    if ($vm.post.contentType === 'markdown') {
                        mditor.value = result.data.content;
                    } else {
                        htmlEditor.summernote("code", result.data.content);
                    }

                    if($vm.post.thumbnail && $vm.post.thumbnail !== ''){
                        $('#dropzone-container').removeClass('hide');
                        $('#dropzone-container').show();
                        $('.dz-image').hide();
                        $('#dropzone').css('background-image', 'url(' + $vm.post.thumbnail + ')');
                        $('#dropzone').css('background-size', 'cover');
                    } else {
                        $('#dropzone-container').hide();
                    }
                },
                afterSuccess: function () {
                    initTagsAndToggle();
                },
                error: function (error) {
                    console.log(error);
                    alert('数据加载失败');
                }
            });
        },
        autoSave: function (callback) {
            var $vm = this;
            let content = $vm.post.contentType === 'markdown' ? mditor.value : htmlEditor.summernote('code');
            if ($vm.post.title !== '' && content !== '') {
                $vm.post.content = content;
                var post = blog.copy($vm.post);
                post.pubdate = moment($('#pubdate').val()).valueOf();
                post.tags = $('#tags').val();
                if (post.status === "2") {
                    post.status = '1';
                }
                blog.put({
                    url: '/admin/posts',
                    data: post,
                    success: function (result) {
                        if (result && result.success) {
                            $vm.post.id = result.data.id;
                            callback && callback();
                        } else {
                            blog.alertError(result.message || '保存文章失败');
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
            var this_ = event.target;
            if (contentType === 'markdown') {
                // 切换为富文本编辑器
                if (mditor.value.length > 0) {
                    htmlEditor.summernote('code', converter.makeHtml(mditor.value));
                }
                mditor.value = '';
                $('#md-container').hide();
                $('#html-container').show();

                this_.innerHTML = '切换为Markdown编辑器';

                this.post.contentType = 'html';
            } else {
                // 切换为markdown编辑器
                if (!htmlEditor.summernote('isEmpty')) {
                    mditor.value = '';
                    mditor.value = converter.makeMarkdown(htmlEditor.summernote('code'));
                }
                $('#html-container').hide();
                $('#md-container').show();

                this.post.contentType = 'markdown';

                this_.innerHTML = '切换为富文本编辑器';
                htmlEditor.summernote("code", "");
            }
        },
        save: function (status) {
            var $vm = this;
            let content = this.post.contentType === 'markdown' ? mditor.value : htmlEditor.summernote('code');
            var title = $vm.post.title;
            if (title === '') {
                blog.alertWarn('请输入文章标题');
                return;
            }
            if (content === '') {
                blog.alertWarn('请输入文章内容');
                return;
            }

            clearInterval(refreshIntervalId);
            $vm.post.status = status;
            $vm.autoSave(function () {
                blog.alertOk({
                    text: '文章保存成功',
                    then: function () {
                        setTimeout(function () {
                            window.location.href = '/admin/posts';
                        }, 500);
                    }
                });
            });
        }
    }
});

function initTagsAndToggle() {
    // Tags Input
    $('#tags').tagsinput({

    });

    $('#commentStatus').bootstrapToggle({
        on: '开启',
        off: '关闭'
    });

    $('#pingStatus').bootstrapToggle({
        on: '开启',
        off: '关闭'
    });

    $('#addThumbnail').bootstrapToggle({
        on: '添加',
        off: '取消'
    });
}

$(document).ready(function () {
    $('#commentStatus').change(function() {
        var checked = $(this).prop('checked');
        if (checked) {
            vm.post.commentStatus = '1';
        } else {
            vm.post.commentStatus = '0';
        }
    });

    $('#pingStatus').change(function() {
        var checked = $(this).prop('checked');
        if (checked) {
            vm.post.pingStatus = '1';
        } else {
            vm.post.pingStatus = '0';
        }
    });

    var thumbdropzone = $('.dropzone');

    // 缩略图上传
    $("#dropzone").dropzone({
        url: "/file",
        filesizeBase: 1024,//定义字节算法 默认1000
        maxFilesize: '15', //MB
        fallback: function () {
            blog.alertError('暂不支持您的浏览器上传!');
        },
        acceptedFiles: 'image/*',
        dictFileTooBig: '您的文件超过15MB!',
        dictInvalidInputType: '不支持您上传的类型',
        thumbnailWidth: 920,
        thumbnailHeight: 360,
        headers: {
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

    $('#addThumbnail').change(function () {
        var checked = $(this).prop('checked');
        if (checked) {
            $('#dropzone-container').removeClass('hide');
            $('#dropzone-container').show();
            var thumbImage = $("#dropzone").css("backgroundImage");
            if(thumbImage && thumbImage.indexOf('url') !== -1){
                thumbImage = thumbImage.split("(")[1].split(")")[0];
                vm.post.thumbnail = thumbImage.substring(1, thumbImage.length - 1);
            }
        } else {
            $('#dropzone-container').addClass('hide');
            vm.post.thumbnail = '';
        }
    });
});
