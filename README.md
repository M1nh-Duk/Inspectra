
<h2>Cách chạy</h2>

- Sử dụng jdk8, thêm lib "tool.jar" ở trong jdk/lib vào project. Sau đó build artifact. 
- Cần phải sửa các phiên bản của thư viện của web framework (tomcat, spring boot) cho giống với version đang chạy của web server nếu không sẽ lỗi crash cả ứng dụng web. (Sửa trong pom.xml của Taint Analysis module)
