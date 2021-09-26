package com.baidu.mobads.demo.main.tools;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.widget.EditText;

public class CustomEditText extends EditText implements MenuItem.OnMenuItemClickListener {
    private static final int ID_SELECT_ALL = android.R.id.selectAll;
    private static final int ID_CUT = android.R.id.cut;
    private static final int ID_COPY = android.R.id.copy;
    private static final int ID_PASTE = android.R.id.paste;

    private final Context mContext;

    public CustomEditText(Context context) {
        super(context);
        this.mContext = context;
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }

    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        menu.add(0, ID_PASTE, 0, "粘贴").setOnMenuItemClickListener(this);
        menu.add(0, ID_CUT, 1, "剪切").setOnMenuItemClickListener(this);
        menu.add(0, ID_COPY, 1, "复制").setOnMenuItemClickListener(this);
        menu.add(0, ID_SELECT_ALL, 1, "全选").setOnMenuItemClickListener(this);
        super.onCreateContextMenu(menu);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return onTextContextMenuItem(item.getItemId());
    }

}