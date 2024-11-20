- Set up tool:
- Sửa đường dẫn của JSP folder trong file Contraints.java
- Chú ý phần pom.xml của taint analysis module phải có tomcat match version của server hoặc ít nhất không chồng chéo (8 vs 9)



- Set up web server project:
- Set up theo hướng dẫn của Smart Tomcat trên mạng bằng Intellij
- Chú ý phải add các dependency sau vào (lấy luôn trong thư mục tomcat/lib rồi add vào project): servlet-api, tomcat-api, tomcat-util, tomcat-util-scan

- Cách chạy tool: Chỉ cần dùng truyền commandline attach sẽ tự động attach vào Tomcat process
