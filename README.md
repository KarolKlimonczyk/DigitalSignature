# DigitalSignature
The sample application allowing to generate digitally signed PDF files. 

## Tutorial
You can find the code and the digital signatures topic explanation on my blog [jvmfy.com](http://jvmfy.com/2018/11/17/how-to-digitally-sign-pdf-files/)

## Used libraries
1. Spring Boot 2.1.0
2. PDFBox 2.0.12
3. Bouncycastle 1.60
4. Gradle 4.6

## How to use
1. Create a keystore with a private and public key pair
2. Add the keystore path and password to the environment variables
3. Change the alias in an *application.yml* file
4. Run an application
5. Go to  http://localhost:8080/api/pdf/export address
6. Save the file
7. Open file by Acrobat Reader

If you have any question visit my blog [jvmfy.com](http://jvmfy.com/2018/11/17/how-to-digitally-sign-pdf-files/). 
