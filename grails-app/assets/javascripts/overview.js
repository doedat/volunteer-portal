//= encoding UTF-8
//  assume jquery
//  assume digivol-image-resize

var shouldRefresh = true;
var refreshTime = 30000;

var previewButtons = $('.btnPreview');
for (var buttonIndex = 0; buttonIndex < previewButtons.length; buttonIndex++) {
    var button = previewButtons[buttonIndex];
    var taskId = button.id;
    button.addEventListener('click', function () {
        $('.btnPreview').attr('disabled', true);
        $(this).addClass('loading');
    });

    button.addEventListener('click', function () {
        shouldRefresh = false;
        bvp.showModal({
            url: '/overview/preview?taskId=' + this.id,
            className: 'lg-bootbox',
            title: 'Task preview',
            backdrop: true,
            buttons: {
                close: {
                    label: 'Close',
                    className: 'btn-default'
                }
            },
            onClose: function () {
                shouldRefresh = true;
            },
            onShown: function () {
                var btn = $('.btnPreview');
                btn.removeClass('loading');
                btn.removeAttr('disabled');
            },
            onContentLoaded: function () {
                setupPanZoom($('#previewBlock img')[0]);
            }
        });

    })
}

// Won't refresh if the preview modal is opened
function refresh() {
    if (shouldRefresh) {
        location.reload();
    }
    setTimeout(refresh, refreshTime);
}

setTimeout(refresh, refreshTime);
