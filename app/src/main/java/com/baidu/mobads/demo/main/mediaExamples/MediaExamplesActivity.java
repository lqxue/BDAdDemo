package com.baidu.mobads.demo.main.mediaExamples;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.baidu.mobads.demo.main.R;
import com.baidu.mobads.demo.main.mediaExamples.novel.ReadActivity;
import com.baidu.mobads.demo.main.mediaExamples.novel.model.bean.CollBookBean;
import com.baidu.mobads.demo.main.mediaExamples.novel.utils.Constant;
import com.baidu.mobads.demo.main.mediaExamples.novel.utils.MD5Utils;
import com.baidu.mobads.demo.main.mediaExamples.novel.utils.StringUtils;
import com.baidu.mobads.demo.main.mediaExamples.novel.utils.ToastUtils;
import com.baidu.mobads.demo.main.mediaExamples.utilsDemo.UtilsFeedsAdActivity;
import com.baidu.mobads.demo.main.mediaExamples.news.NewsDemoActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MediaExamplesActivity extends Activity {

    private List<CollBookBean> collBookBeans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_examples);

        File outFile = new File(getFilesDir(), "斗罗大陆.txt");
        if (!outFile.exists()) {
            try {
                InputStream is = getAssets().open("testDemo.txt");
                FileOutputStream fos = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1){
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<File> files = new ArrayList<>();
        files.add(outFile);
        collBookBeans = convertCollBook(files);


        initview();
    }

    private void initview() {

        // 工具类接入
        Button toolsButton = this.findViewById(R.id.tools_example);
        toolsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaExamplesActivity.this, UtilsFeedsAdActivity.class);
                startActivity(intent);
            }
        });

        // 书籍类接入
        Button bookButton = this.findViewById(R.id.book_example);
        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // icCollected = true 表示小说来自于SD卡
                ReadActivity.startActivity(MediaExamplesActivity.this,
                        collBookBeans.get(0), true);

            }
        });

        // 资讯类接入
        Button newsButton = this.findViewById(R.id.news_example);
        newsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaExamplesActivity.this, NewsDemoActivity.class);
                startActivity(intent);
            }
        });

    }

    /**
     * 将文件转换成CollBook
     * @param files:需要加载的文件列表
     * @return
     */
    private List<CollBookBean> convertCollBook(List<File> files){
        List<CollBookBean> collBooks = new ArrayList<>(files.size());
        for(File file : files){
            //判断文件是否存在
            if (!file.exists()) continue;

            CollBookBean collBook = new CollBookBean();
            collBook.set_id(MD5Utils.strToMd5By16(file.getAbsolutePath()));
            collBook.setTitle(file.getName().replace(".txt",""));
            collBook.setAuthor("");
            collBook.setShortIntro("无");
            collBook.setCover(file.getAbsolutePath());
            collBook.setLocal(true);
            collBook.setLastChapter("开始阅读");
            collBook.setUpdated(StringUtils.dateConvert(file.lastModified(), Constant.FORMAT_BOOK_DATE));
            collBook.setLastRead(StringUtils.
                    dateConvert(System.currentTimeMillis(), Constant.FORMAT_BOOK_DATE));
            collBooks.add(collBook);
        }
        return collBooks;
    }
}
