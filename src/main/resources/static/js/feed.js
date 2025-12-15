
let selectedCompany = null;

const companyList = document.getElementById("company-list");
const topicSection = document.getElementById("topic-section");
const topicList = document.getElementById("topic-list");
const topicTitle = document.getElementById("topic-title");
const resultSection = document.getElementById("result-section");
const articleList = document.getElementById("article-list");

/* 회사 목록 로드 */
API.getCompanies().then(companies => {
  companies.forEach(c => {
    const btn = document.createElement("button");
    btn.textContent = c.name;
    btn.onclick = () => selectCompany(c);
    companyList.appendChild(btn);
  });
});

function selectCompany(company) {
  selectedCompany = company;
  topicSection.style.display = "block";
  topicTitle.textContent = `Choose what you want to know about ${company.name}`;
  topicList.innerHTML = "";

  API.getTopics().then(topics => {
    topics.forEach(t => {
      const btn = document.createElement("button");
      btn.textContent = t.name;
      btn.onclick = () => selectTopic(t);
      topicList.appendChild(btn);
    });
  });
}

function selectTopic(topic) {
  resultSection.style.display = "block";
  articleList.innerHTML = "";

  API.searchNews(selectedCompany.name, topic.id)
    .then(articles => {
      articles.forEach(a => {
        const div = document.createElement("div");
        div.innerHTML = `<a href="${a.url}" target="_blank">${a.title}</a>`;
        articleList.appendChild(div);
      });
    });
}
