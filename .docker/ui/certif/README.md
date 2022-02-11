## Setting Up HTTPS for localhost
### STEP 1: generate self-signed certification
```SHELL
openssl req -x509 -sha256 -nodes -newkey rsa:2048 -days 36500 -keyout cert.key -out cert.crt -subj "/C=FR/ST=France/L=Paris/O=Chutney/CN=localhost"  -addext "subjectAltName = DNS:localhost"
```
The generated certificate(in the current directory) will be in x509 container format with SHA256 signature algorithm, 2048bit RSA authentication key and is valid for **100 years**.
### STEP 2: Trust authority of the certificate
When browsers get the certificate from server, the authenticity is verified by checking with existing CAs.
Browser has a list of trusted CAs by default, if the certificate issuer is not there, then browser will be showing a security warning ‘untrusted connection’.
Our generated certificate is self-signed, so browser will give security warning. In order to bypass that, we have to **manually** verify the trust of certificate by **importing it to the Trusted Root Certification Authorities of our browser**.
