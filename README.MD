### 视频裁剪以及视频压缩 ，使用ffmpeg，速度还是较快的，清晰度高，压缩后比较小

#### 使用如下


- Step 1. Add the JitPack repository to your build file

```
        allprojects {
                repositories {
                        ...
                        maven { url 'https://jitpack.io' }
                }
        }
```

- Step 2. Add the dependency
```
        dependencies {
                        implementation 'com.github.alguojian:VideoFfmpeg:v1.1.3'
        }
```

##### 1、初始化 ，需要拿到上下文存储视频或者截图到app私有缓存路径中

```
 //application或者进入activity后，是否打开日志
 
 VideoFfmpeg.init(this, true)
 
 //监听视频操作
 VideoFfmpeg.setListener(object :VfVideoListener{
            
            //返回视频裁剪或者压缩路径，以及封面路径(截取第一帧或者压缩后才会返回)
            override fun onFinish(videoUrl: String?, thumbImage: String?) {
            }
            
            //压缩进度
            override fun onProgress(progress: Int) {
            }

            override fun onError() {
            }

            override fun onStart() {
            }
        })

 //点击完成时的回调，视频已经裁剪和获取封面图成功路径如下
 
 VfVideoStatusBean.getThumbImagePath()
 VfVideoStatusBean.getVideoPath()
 
 
 VideoFfmpeg.setSaveClickListener(1, status -> {
            if (status==1){
                
            }
        });
  
 //视频操作完成需要调用，之后才可以操作下一个视频
 VideoFfmpeg.changeStatus()

 //所以每次操作视频前需要判断      
 if (!VideoFfmpeg.isOperating){
     
     }
   
 
```

##### 2、页面结束是可以调用，取消视频的操作等
```
 override fun onDestroy() {
        super.onDestroy()
        VideoFfmpeg.onDestroy()
    }
```

##### 3、打开视频选择页面，进行选择视频，裁剪视频，压缩视频等一系列操作

```
VideoFfmpeg.openSelectVideo(this)
```

##### 4、其他用法

```

//压缩视频
VideoFfmpeg.startCompress("")

//获取封面图
VideoFfmpeg.startInterceptCover("")

//视频裁剪
VideoFfmpeg.startCrop("",VideoUtils.videoCropOutPath,1000L,2000L)

```

- 参考

- https://blog.csdn.net/mabeijianxi/article/details/63335722
- https://blog.csdn.net/leixiaohua1020/article/details/18893769


