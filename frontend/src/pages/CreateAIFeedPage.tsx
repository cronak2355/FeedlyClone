import React, { useState, useEffect } from 'react'

interface Keyword {
  id: string
  desc: string
}

interface KeywordsData {
  [group: string]: Keyword[]
}

interface Props {
  keywordsData: KeywordsData
}

export default function CreateAIFeedPage({ keywordsData = {} }: Props) {
  console.log('키워드 데이터:', keywordsData);
  const mainKeywords = Object.keys(keywordsData)

  const [selectedMain1, setSelectedMain1] = useState<string | null>(null)
  const [selectedSub1, setSelectedSub1] = useState<Keyword | null>(null)

  const [selectedMain2, setSelectedMain2] = useState<string | null>(null)
  const [selectedSub2, setSelectedSub2] = useState<Keyword | null>(null)

  const [newsResults, setNewsResults] = useState<any[]>([])

  // 두 키워드가 모두 선택되면 뉴스 검색 API 호출
  useEffect(() => {
    if (selectedSub1 && selectedSub2) {
      fetch('/api/news', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          keyword1: selectedSub1.id,
          keyword2: selectedSub2.id,
        }),
      })
        .then(res => res.json())
        .then(data => {
            setNewsResults(data.articles || data || [])
        })
        .catch(err => console.error(err))
    }
  }, [selectedSub1, selectedSub2])

  return (
    <div>
      <h2>첫 번째 키워드 선택</h2>
      <select onChange={e => setSelectedMain1(e.target.value)} value={selectedMain1 || ''}>
        <option value="">메인 키워드 선택</option>
        {mainKeywords.map(mk => (
          <option key={mk} value={mk}>{mk}</option>
        ))}
      </select>

      {selectedMain1 && (
        <>
          <h3>{selectedMain1} 하위 키워드</h3>
          <select
            onChange={e => {
              const kw = keywordsData[selectedMain1].find(k => k.id === e.target.value)
              setSelectedSub1(kw || null)
            }}
            value={selectedSub1?.id || ''}
          >
            <option value="">하위 키워드 선택</option>
            {keywordsData[selectedMain1].map(k => (
              <option key={k.id} value={k.id}>
                {k.id} - {k.desc}
              </option>
            ))}
          </select>
        </>
      )}

      <hr />

      <h2>두 번째 키워드 선택</h2>
      <select onChange={e => setSelectedMain2(e.target.value)} value={selectedMain2 || ''}>
        <option value="">메인 키워드 선택</option>
        {mainKeywords.map(mk => (
          <option key={mk} value={mk}>{mk}</option>
        ))}
      </select>

      {selectedMain2 && (
        <>
          <h3>{selectedMain2} 하위 키워드</h3>
          <select
            onChange={e => {
              const kw = keywordsData[selectedMain2].find(k => k.id === e.target.value)
              setSelectedSub2(kw || null)
            }}
            value={selectedSub2?.id || ''}
          >
            <option value="">하위 키워드 선택</option>
            {keywordsData[selectedMain2].map(k => (
              <option key={k.id} value={k.id}>
                {k.id} - {k.desc}
              </option>
            ))}
          </select>
        </>
      )}

      <hr />

      <h2>선택한 키워드</h2>
      <p>1: {selectedSub1 ? `${selectedSub1.id} (${selectedSub1.desc})` : '없음'}</p>
      <p>2: {selectedSub2 ? `${selectedSub2.id} (${selectedSub2.desc})` : '없음'}</p>

      <hr />

      <h2>뉴스 결과</h2>
      {newsResults.length === 0 && <p>뉴스가 없습니다.</p>}
      <ul>
        {newsResults.map((article, idx) => (
          <li key={idx}>
            <a href={article.url} target="_blank" rel="noopener noreferrer">
              {article.title}
            </a>
            <p>{article.description}</p>
          </li>
        ))}
      </ul>
    </div>
  )
}
