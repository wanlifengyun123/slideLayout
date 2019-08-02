package com.example.phone_server.http;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.async.http.body.Part;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class NIOHttpServer {

    private static final String TAG = "NIOHttpServer";

    private static final String TEXT_CONTENT_TYPE = "text/html;charset=utf-8";
    private static final String CSS_CONTENT_TYPE = "text/style;charset=utf-8";
    private static final String BINARY_CONTENT_TYPE = "application/octet-stream";
    private static final String JS_CONTENT_TYPE = "application/javascript";
    private static final String PNG_CONTENT_TYPE = "application/x-png";
    private static final String JPG_CONTENT_TYPE = "application/jpeg";
    private static final String SWF_CONTENT_TYPE = "application/x-shockwave-flash";
    private static final String WOFF_CONTENT_TYPE = "application/x-font-woff";
    private static final String TTF_CONTENT_TYPE = "application/x-font-truetype";
    private static final String SVG_CONTENT_TYPE = "image/svg+xml";
    private static final String EOT_CONTENT_TYPE = "image/vnd.ms-fontobject";
    private static final String MP3_CONTENT_TYPE = "audio/mp3";
    private static final String MP4_CONTENT_TYPE = "video/mpeg4";

    private static NIOHttpServer mInstance;

    public static int PORT = 54321;

    private AsyncHttpServer mHttpServer;

    private AsyncServer mAsyncServer;

    private Context mContext;

    private File mTransferFile = new File(Environment.getExternalStorageDirectory() + File.separator + "WifiTransfer");
    private File mRecievedFile;
    private BufferedOutputStream mFileOutPutStream;

    public static NIOHttpServer getInstance() {
        if (mInstance == null) {
            // 增加类锁,保证只初始化一次
            synchronized (NIOHttpServer.class) {
                if (mInstance == null) {
                    mInstance = new NIOHttpServer();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        mContext = context;
        mHttpServer = new AsyncHttpServer();
        mAsyncServer = new AsyncServer();
        sendHtmlToPC();
        mHttpServer.listen(mAsyncServer, PORT);
    }

    private void sendHtmlToPC() {
        mHttpServer.get("/images/.*", new NIOHttpServerRequestCallback(1));
        mHttpServer.get("/js/.*", new NIOHttpServerRequestCallback(2));
        mHttpServer.get("/style/.*", new NIOHttpServerRequestCallback(3));
        mHttpServer.get("/", new NIOHttpServerRequestCallback(4));
        //upload
        mHttpServer.post("/?", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, final AsyncHttpServerResponse response) {
                final MultipartFormDataBody body = (MultipartFormDataBody) request.getBody();
                body.setMultipartCallback(new MultipartFormDataBody.MultipartCallback() {
                    @Override
                    public void onPart(Part part) {
                        if (part.isFile()) {
                            body.setDataCallback(new DataCallback() {
                                @Override
                                public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                                    // 开始传输文件
                                    if (mFileOutPutStream != null) {
                                        try {
                                            mFileOutPutStream.write(bb.getAllByteArray());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    bb.recycle();
                                }
                            });
                        } else {
                            if (body.getDataCallback() == null) {
                                body.setDataCallback(new DataCallback() {
                                    @Override
                                    public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                                        //还没设置文件名
                                        try {
                                            String fileName = URLDecoder.decode(new String(bb.getAllByteArray()), "UTF-8");
                                            if (!mTransferFile.exists()) {
                                                boolean b = mTransferFile.mkdirs();
                                            }
                                            mRecievedFile = new File(mTransferFile, fileName);
                                            try {
                                                mFileOutPutStream = new BufferedOutputStream(new FileOutputStream(mRecievedFile));
                                            } catch (FileNotFoundException e) {
                                                e.printStackTrace();
                                            }
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                        bb.recycle();
                                    }
                                });
                            }
                        }
                    }
                });
                request.setEndCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        if (mFileOutPutStream != null) {
                            try {
                                mFileOutPutStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        mFileOutPutStream = null;
                        response.end();
                    }
                });
            }
        });
    }

    public void onDestroy() {
        if (mHttpServer != null) {
            mHttpServer.stop();
        }

        if (mAsyncServer != null) {
            mAsyncServer.stop();
        }

        if (mFileOutPutStream != null) {
            try {
                mFileOutPutStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mFileOutPutStream = null;
    }

    class NIOHttpServerRequestCallback implements HttpServerRequestCallback {

        private int mSendType;

        NIOHttpServerRequestCallback(int type) {
            mSendType = type;
        }

        @Override
        public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
            Log.d(TAG, "request:" + request.getPath());
            try {
                switch (mSendType) {
                    case 1: //image
                    case 2: //js
                    case 3: //style
                        sendResource(request, response);
                        break;
                    case 4: //网页
                        response.send(getIndexContent());
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                response.code(500).end();
            }
        }

        private String getIndexContent() throws IOException {
            BufferedInputStream inputStream = null;
            try {
                inputStream = new BufferedInputStream(mContext.getAssets().open("index.html"));
                ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
                int len;
                byte[] tmp = new byte[10240];
                while ((len = inputStream.read(tmp)) > 0) {
                    byteOS.write(tmp, 0, len);
                }
                return new String(byteOS.toByteArray(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void sendResource(AsyncHttpServerRequest request, AsyncHttpServerResponse response) throws IOException {
            String fullPath = request.getPath();
            fullPath = fullPath.replace("%20", " ");
            String resourceName = fullPath;
            if (resourceName.startsWith("/")) {
                resourceName = resourceName.substring(1);
            }
            if (resourceName.indexOf("?") > 0) {
                resourceName = resourceName.substring(0, resourceName.indexOf("?"));
            }
            if (!TextUtils.isEmpty(getContentTypeByResourceName(resourceName))) {
                response.setContentType(getContentTypeByResourceName(resourceName));
            }
            BufferedInputStream bInputStream = new BufferedInputStream(mContext.getAssets().open(resourceName));
            response.sendStream(bInputStream, bInputStream.available());
        }

        private String getContentTypeByResourceName(String resourceName) {
            if (resourceName.endsWith(".style")) {
                return CSS_CONTENT_TYPE;
            } else if (resourceName.endsWith(".js")) {
                return JS_CONTENT_TYPE;
            } else if (resourceName.endsWith(".swf")) {
                return SWF_CONTENT_TYPE;
            } else if (resourceName.endsWith(".png")) {
                return PNG_CONTENT_TYPE;
            } else if (resourceName.endsWith(".jpg") || resourceName.endsWith(".jpeg")) {
                return JPG_CONTENT_TYPE;
            } else if (resourceName.endsWith(".woff")) {
                return WOFF_CONTENT_TYPE;
            } else if (resourceName.endsWith(".ttf")) {
                return TTF_CONTENT_TYPE;
            } else if (resourceName.endsWith(".svg")) {
                return SVG_CONTENT_TYPE;
            } else if (resourceName.endsWith(".eot")) {
                return EOT_CONTENT_TYPE;
            } else if (resourceName.endsWith(".mp3")) {
                return MP3_CONTENT_TYPE;
            } else if (resourceName.endsWith(".mp4")) {
                return MP4_CONTENT_TYPE;
            }
            return "";
        }
    }
}
