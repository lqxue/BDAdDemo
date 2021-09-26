package com.baidu.mobads.demo.main.mediaExamples.common.parser;

import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobads.demo.main.mediaExamples.news.bean.FeedItem;
import com.baidu.mobads.demo.main.mediaExamples.news.utils.FeedParseHelper;
import com.baidu.mobads.demo.main.mediaExamples.news.utils.IdIterator;

public class FeedParser implements Parser<NativeResponse, FeedItem> {
    final private IdIterator idIterator;

    public FeedParser(IdIterator idIterator) {
        this.idIterator = idIterator;
    }

    public FeedItem parseData(NativeResponse nrAd) {
        return FeedParseHelper.parseItemFromResponse(nrAd, idIterator.next());
    }
}
