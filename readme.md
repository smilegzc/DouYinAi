自动识别抖音上漂亮小姐姐，自动点赞并关注
<h4>文件说明：</h4>
<h5>AiBot:</h5>
负责与手机交互，获取屏幕截图，将截图提交给AiFace进行检测。由AiFace类返回信息决定是否点赞并关注。
<h5>AiFace:</h5>
负责处理图片，进行人脸识别。
将图片转换为BASE64编码，由HttpWeb类上传到服务器（此处使用腾讯AI）进行检测获并取人物信息。
<h5>HttpWeb:</h5>
负责网络传输，采用post方式提交参数