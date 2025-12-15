// TypeScript types matching backend DTOs

export interface FeedItem {
    title: string;
    link: string;
    description?: string;
    author?: string;
    publishedDate: string;
    thumbnailUrl?: string;
    category?: string;
}

export interface Feed {
    title: string;
    feedUrl: string;
    siteUrl?: string;
    description?: string;
    category?: string;
    faviconUrl?: string;
    subscriberCount?: number;
    feedType?: string;
    isFollowed: boolean;
}

export interface Article {
    url: string;
    title: string;
    description?: string;
    thumbnailUrl?: string;
    siteName?: string;
    isSaved?: boolean;
    isRead?: boolean;
}

export interface ApiResponse<T> {
    success: boolean;
    message?: string;
    data?: T;
}
