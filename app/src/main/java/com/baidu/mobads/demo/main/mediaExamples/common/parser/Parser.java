package com.baidu.mobads.demo.main.mediaExamples.common.parser;

public interface Parser<Source, Target> {
    public Target parseData(Source data);
}
