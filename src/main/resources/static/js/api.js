const API = {
  getCompanies() {
    return fetch("/api/keywords/company")
      .then(res => res.json());
  },

  getTopics() {
    return fetch("/api/keywords/topic")
      .then(res => res.json());
  },

  searchNews(company, topicId) {
    return fetch(`/api/news/search?company=${company}&topicId=${topicId}`)
      .then(res => res.json());
  }
};
