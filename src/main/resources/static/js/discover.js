/**
 * Feedly Clone - Discover Page JavaScript
 * AJAX Follow/Unfollow, CSRF 보안, 미리보기 등
 */

$(document).ready(function() {
    // CSRF 토큰 설정
    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    // 모든 AJAX 요청에 CSRF 토큰 추가
    $.ajaxSetup({
        beforeSend: function(xhr) {
            if (csrfHeader && csrfToken) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            }
        }
    });

    // 팔로잉 카운트 초기화
    updateFollowingCount();

    // === Follow 버튼 클릭 ===
    $(document).on('click', '.btn-follow', function(e) {
        e.preventDefault();
        const $btn = $(this);
        const feedUrl = $btn.data('feed-url');
        const title = $btn.data('title');
        const description = $btn.data('description');
        const faviconUrl = $btn.data('favicon');
        const category = $btn.data('category');
        const feedType = $btn.data('feed-type') || 'RSS';

        // 버튼 비활성화 및 로딩 표시
        const originalHtml = $btn.html();
        $btn.prop('disabled', true)
            .html('<span class="spinner-border spinner-border-sm me-1"></span>Following...');

        $.ajax({
            url: '/discover/follow',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                feedUrl: feedUrl,
                title: title,
                description: description,
                faviconUrl: faviconUrl,
                category: category,
                feedType: feedType
            }),
            success: function(response) {
                if (response.success) {
                    showToast('success', response.message);
                    
                    // 버튼을 Unfollow로 변경
                    $btn.removeClass('btn-success btn-follow')
                        .addClass('btn-outline-danger btn-unfollow')
                        .html('<i class="bi bi-x-circle me-1"></i> Unfollow')
                        .prop('disabled', false);
                    
                    // 카드에 팔로잉 스타일 추가
                    $btn.closest('.feed-card, .reddit-card').addClass('feed-card-following');
                    
                    // 팔로잉 뱃지 추가
                    const $header = $btn.closest('.card-body').find('.d-flex').first();
                    if (!$header.find('.badge.bg-success').length) {
                        $header.append('<span class="badge bg-success ms-2"><i class="bi bi-check-lg"></i></span>');
                    }
                    
                    updateFollowingCount();
                } else {
                    showToast('warning', response.message);
                    $btn.prop('disabled', false).html(originalHtml);
                }
            },
            error: function(xhr) {
                const message = xhr.responseJSON?.message || '오류가 발생했습니다.';
                showToast('error', message);
                $btn.prop('disabled', false).html(originalHtml);
            }
        });
    });

    // === Unfollow 버튼 클릭 ===
    $(document).on('click', '.btn-unfollow', function(e) {
        e.preventDefault();
        const $btn = $(this);
        const feedUrl = $btn.data('feed-url');

        if (!confirm('이 피드를 언팔로우하시겠습니까?')) {
            return;
        }

        const originalHtml = $btn.html();
        $btn.prop('disabled', true)
            .html('<span class="spinner-border spinner-border-sm me-1"></span>...');

        $.ajax({
            url: '/discover/unfollow',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ feedUrl: feedUrl }),
            success: function(response) {
                if (response.success) {
                    showToast('success', response.message);
                    
                    // 버튼을 Follow로 변경
                    $btn.removeClass('btn-outline-danger btn-unfollow')
                        .addClass('btn-success btn-follow')
                        .html('<i class="bi bi-plus-circle me-1"></i> Follow')
                        .prop('disabled', false);
                    
                    // 카드에서 팔로잉 스타일 제거
                    $btn.closest('.feed-card, .reddit-card').removeClass('feed-card-following');
                    
                    // 팔로잉 뱃지 제거
                    $btn.closest('.card-body').find('.badge.bg-success').remove();
                    
                    updateFollowingCount();
                } else {
                    showToast('warning', response.message);
                    $btn.prop('disabled', false).html(originalHtml);
                }
            },
            error: function(xhr) {
                const message = xhr.responseJSON?.message || '오류가 발생했습니다.';
                showToast('error', message);
                $btn.prop('disabled', false).html(originalHtml);
            }
        });
    });

    // === 미리보기 버튼 클릭 ===
    $(document).on('click', '.btn-preview', function() {
        const feedUrl = $(this).data('feed-url');
        loadFeedPreview(feedUrl);
    });

    // === 모달 미리보기에서 Follow 버튼 ===
    $('#preview-follow-btn').on('click', function() {
        const feedUrl = $(this).data('feed-url');
        const title = $(this).data('title');
        const description = $(this).data('description');
        
        // Follow 요청 실행
        $.ajax({
            url: '/discover/follow',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                feedUrl: feedUrl,
                title: title,
                description: description
            }),
            success: function(response) {
                if (response.success) {
                    showToast('success', response.message);
                    $('#previewModal').modal('hide');
                    
                    // 해당 피드 카드의 버튼 상태 업데이트
                    $(`.btn-follow[data-feed-url="${feedUrl}"]`)
                        .removeClass('btn-success btn-follow')
                        .addClass('btn-outline-danger btn-unfollow')
                        .html('<i class="bi bi-x-circle me-1"></i> Unfollow');
                    
                    updateFollowingCount();
                } else {
                    showToast('warning', response.message);
                }
            },
            error: function(xhr) {
                showToast('error', xhr.responseJSON?.message || '오류가 발생했습니다.');
            }
        });
    });
});

/**
 * 팔로잉 카운트 업데이트
 */
function updateFollowingCount() {
    $.ajax({
        url: '/discover/following',
        method: 'GET',
        success: function(response) {
            if (response.success && response.data) {
                const count = response.data.length;
                $('#following-count span').text(count);
            }
        },
        error: function() {
            console.debug('Failed to update following count');
        }
    });
}

/**
 * 피드 미리보기 로드
 */
function loadFeedPreview(feedUrl) {
    const $loading = $('#preview-loading');
    const $content = $('#preview-content');
    const $error = $('#preview-error');
    const $followBtn = $('#preview-follow-btn');

    // 초기 상태 설정
    $loading.removeClass('d-none');
    $content.addClass('d-none');
    $error.addClass('d-none');
    $followBtn.prop('disabled', true);

    $.ajax({
        url: '/discover/preview',
        method: 'GET',
        data: { feedUrl: feedUrl },
        success: function(response) {
            $loading.addClass('d-none');
            
            if (response.success && response.data) {
                const feed = response.data;
                
                $('#preview-title').text(feed.title);
                $('#preview-description').text(feed.description || '');
                
                // 아이템 목록
                const $items = $('#preview-items').empty();
                if (feed.items && feed.items.length > 0) {
                    feed.items.forEach(function(item) {
                        const date = item.publishedDate 
                            ? new Date(item.publishedDate).toLocaleDateString() 
                            : '';
                        $items.append(`
                            <li class="list-group-item">
                                <a href="${item.link}" target="_blank" class="text-decoration-none">
                                    <div class="fw-medium">${escapeHtml(item.title)}</div>
                                    <small class="text-muted">
                                        ${item.author ? '<i class="bi bi-person me-1"></i>' + escapeHtml(item.author) : ''}
                                        ${date ? '<span class="ms-2"><i class="bi bi-calendar me-1"></i>' + date + '</span>' : ''}
                                    </small>
                                </a>
                            </li>
                        `);
                    });
                }
                
                // Follow 버튼 상태 설정
                $followBtn.data('feed-url', feed.feedUrl)
                          .data('title', feed.title)
                          .data('description', feed.description)
                          .prop('disabled', feed.isFollowed);
                
                if (feed.isFollowed) {
                    $followBtn.html('<i class="bi bi-check-lg me-1"></i> 팔로잉');
                } else {
                    $followBtn.html('<i class="bi bi-plus-circle me-1"></i> Follow');
                }
                
                $content.removeClass('d-none');
            } else {
                $error.removeClass('d-none');
            }
        },
        error: function() {
            $loading.addClass('d-none');
            $error.removeClass('d-none');
        }
    });
}

/**
 * 토스트 알림 표시
 */
function showToast(type, message) {
    const $toast = $('#toast-notification');
    const $body = $toast.find('.toast-body');
    
    // 타입별 스타일
    $toast.removeClass('bg-success bg-danger bg-warning text-white');
    
    switch(type) {
        case 'success':
            $toast.addClass('bg-success text-white');
            break;
        case 'error':
            $toast.addClass('bg-danger text-white');
            break;
        case 'warning':
            $toast.addClass('bg-warning');
            break;
    }
    
    $body.text(message);
    
    const toast = new bootstrap.Toast($toast[0], {
        autohide: true,
        delay: 3000
    });
    toast.show();
}

/**
 * HTML 이스케이프
 */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
