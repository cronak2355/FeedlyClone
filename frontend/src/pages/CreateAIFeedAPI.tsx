import React, { useEffect, useState } from 'react';
import CreateAIFeedPage from './CreateAIFeedPage';

interface Keyword {
  id: string;
  desc: string;
}

interface KeywordsData {
  [group: string]: Keyword[];
}

export default function CreateAIFeedAPI() {
  const [keywordsData, setKeywordsData] = useState<KeywordsData>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('/api/keywords')
      .then(res => res.json())
      .then(data => {
        setKeywordsData(data);
        setLoading(false);
      })
      .catch(err => {
        console.error('키워드 데이터를 불러오는데 실패했습니다.', err);
        setLoading(false);
      });
  }, []);

  if (loading) return <div>키워드 데이터를 불러오는 중...</div>;
  if (Object.keys(keywordsData).length === 0) return <div>키워드 데이터가 없습니다.</div>;

  return <CreateAIFeedPage keywordsData={keywordsData} />;
}
