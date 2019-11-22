# point

1、格式中csd-0、csd-1

（1）在视频中指sps pps 

  这个可以在MediaCodec的outputFormat获得

（2）在音频中指ADST 

  在每一帧前面插入7位 
  
  2、YUV格式
  
|YUV420|YUV422|YUV444|
|---|---|---|
|四个Y共用一对UV|两个Y公用一对UV|一个Y用一对UV|

|||
|---|---|
|I420: YYYYYYYY UU VV |   YUV420P|
|YV12: YYYYYYYY VV UU |   YUV420P|
|NV12: YYYYYYYY UVUV  |   YUV420SP|
|NV21: YYYYYYYY VUVU  |   YUV420SP|
  
YUV格式有两大类：planar和packed。

对于planar的YUV格式，先连续存储所有像素点的Y，紧接着存储所有像素点的U，随后是所有像素点的V。

对于packed的YUV格式，每个像素点的Y,U,V是连续交叉存储的。
