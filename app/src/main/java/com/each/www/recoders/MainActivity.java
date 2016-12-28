package com.each.www.recoders;


import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.provider.CallLog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//设计思路：1.检索录音目录，用列表展示 2.点击item播放录音内容   ok

/**
 *  升级：0.获取通话记录，根据记录时间匹配录音文件                   ok
 *        1.显示播放进度
 *        2.改变当前item颜色（以区分）
 *        3.长按可以删除该item(录音文件)
 */
public class MainActivity extends AppCompatActivity implements OnItemClickListener,AdapterView.OnItemLongClickListener{
    private File recorderPath;
    private ListView recorderList;
    private MediaPlayer player = new MediaPlayer();

    private long dateLong;
    private String msg;
    private List<Map<String, Object>> listitem;
    private Map<String, Object> showitem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listitem = new ArrayList<Map<String, Object>>();

        recorderList = (ListView)findViewById(R.id.recorderList);
        recorderPath = new File("/storage/emulated/0/Recorder/call");
        getAllFiles(recorderPath);

        //创建一个simpleAdapter
        SimpleAdapter myAdapter = new SimpleAdapter(getApplicationContext(),listitem,
                R.layout.recorder_item,new String[]{"l_msg","r_icon","r_name","r_time"},
                new int[]{R.id.l_msg,R.id.r_icon,R.id.r_name,R.id.r_time});
        ListView recorderList = (ListView)findViewById(R.id.recorderList);
        recorderList.setAdapter(myAdapter);
        recorderList.setOnItemClickListener(this);
        //recorderList.setOnTouchListener(this);//或许这个替代更好
    }

    @Override
    protected void onDestroy() {
        if (player != null){
            player.stop();
            player.reset();
            player.release();
            player = null;
        }
        super.onDestroy();
    }

    private void getAllFiles(File path2){
        File files[] = path2.listFiles();
        if(files != null){
            for (File f : files){
                if(f.isDirectory()){
                    getAllFiles(f);
                }else{

                   Log.e("TAG",f.toString());
                    ContentResolver resolver = getContentResolver();
                    Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, // 查询通话记录的URI
                            new String[] { CallLog.Calls.CACHED_NAME
                            // 通话记录的联系人
                    , CallLog.Calls.NUMBER// 通话记录的电话号码
                    , CallLog.Calls.DATE// 通话记录的日期
                    , CallLog.Calls.DURATION// 通话时长
                    , CallLog.Calls.TYPE }// 通话类型
                    , null, null, CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
                    );
                    while (cursor.moveToNext())//这个遍历真吃力
                       {

                           dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                           int c_duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION))*1000*60;
                          // String c_number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                           String c_name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                           if (dateLong <= f.lastModified() && (dateLong+c_duration)>=f.lastModified()){
                               Log.e("TAG",f.getName()+"测试。。。。。。");
                               Log.e("TAG","联系人 "+c_name);
                               String c_number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                               Log.e("TAG","电话 "+c_number);
                               if (c_name.isEmpty()){
                                   msg = c_number;
                               }else if (!c_name.isEmpty()){
                                   msg = c_name+":"+c_number;
                               }

                           }


                           /*String c_name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                           String c_number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                           String c_date = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date(dateLong));

                           int c_duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION))/60;
                           String c_durationString = c_duration+"分钟";
                           int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));*/


                    }

                    //确定拿到录音文件
                    SimpleDateFormat sdf= new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                    String time =sdf.format(new Date(f.lastModified()));
                    String name = f.getName();
                    //Log.e("TAG", "录音时间  "+time);
                    //Log.e("TAG","文件名字  "+name);

                    showitem = new HashMap<String, Object>();
                    showitem.put("r_icon", R.drawable.r_icon);
                    showitem.put("l_msg",msg);
                    showitem.put("r_name", name);
                    showitem.put("r_time", time);

                    listitem.add(showitem);
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (player.isPlaying()){
            player.stop();
            player.reset();
            player.release();
            player = null;
        }
            player = new MediaPlayer();
            //Toast.makeText(MainActivity.this, "测试", Toast.LENGTH_SHORT).show();
            TextView r_name = (TextView) view.findViewById(R.id.r_name);
            Log.e("TAG", r_name.getText().toString());
            //文件名
            String R_name = r_name.getText().toString();
            //文件全路径
            String R_apath = recorderPath.toString() + "/" + R_name;
            try {
                player.setDataSource(R_apath);
                player.prepare();
                player.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(MainActivity.this, "长按", Toast.LENGTH_SHORT).show();
        return true;
    }
}
