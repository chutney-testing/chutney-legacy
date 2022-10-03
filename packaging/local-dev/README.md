## 1- Encrypt / Decrypt placeholders in configuration file:
We are using [jasypt-spring-boot](https://github.com/ulisesbocchio/jasypt-spring-boot) to encrypt some properties in spring configuration files.

### 1.a - Encrypt
- In config file, wrap property value with **DEC** function

    Example:

    ```yaml
    chutney:
      security:
        users:
          -
            id: user
            name: user
            firstname: user
            lastname: user
            mail: user@user.com
            password: DEC(user)
    ```
- Run encrypt goal:

  ```shell
  mvn jasypt:encrypt -Djasypt.encryptor.public-key-location="file:{path_to_public_key}"  -Djasypt.encryptor.public-key-format="PEM" -Djasypt.plugin.path="file:{path_to_config_file}"
  ```

    Example:

    ```shell
    mvn jasypt:encrypt  -Djasypt.encryptor.public-key-location="file:src/main/resources/security/public.pem"  -Djasypt.encryptor.public-key-format="PEM" -Djasypt.plugin.path="file:src/main/resources/application.yml"
    ```
- See result in config file:
    ```yaml
  ssl:
      keyStore: 'classpath:security/https/server.jks'
      key-store-password: DEC(server)
      key-password: DEC(server)
      trust-store: 'classpath:security/https/truststore.jks'
      trust-store-password: DEC(truststore)
    ```


### 1.b - Decrypt
- In config file, wrap encrypted property value with **ENC** function

  Example:

    ```yaml
  ssl:
      keyStore: 'classpath:security/https/server.jks'
      key-store-password: ENC(Gbx7KiGNL+a17iZ8ZhAy2FbtJHZ7hKOdsor+xG29Ug5S1MGvxdMMCs2tYPIQK9shG5cVWxrIghg9ugujMjtNFYNdYDO6YbWENAuXWP12H50Z/29Iz7zymZTUC3VNI3WBMmPXj7ZUiJ5b5w2PY/OXEPCnMHkR+ARIS5S61nTIyXGbt1mgAVqcobn6TU1ywxq9uT+Y3H1mx4soJxV58Gfy7m0LT8d0dknPt8TUJ71cwz8wrb22HjbAofQTAxzd1lZrl3ugdN2IRIpr1XjMg2l9RYd+ezMnsAc8arReBifuEdVJwD72Eqr96HPp3UtoRyzWbHheP9LXplZo6yDj4NGmvA==)
      key-password: ENC(KBtL7IdLlchJCZimeeSolv5AxCPXrMjQ4G30/ZLbCV+tHlgqkyCbOf/S2qz0p4SqTdXiVtXskF2z/fM6R0VETwsQHLQFS8KIDVvoF1sdgJKzjFQZtZdI4DR2O+ah/wsQ0Xyyu+fbD1oPjUwVMt4JSBQxUGJajAOiXWMrAPdVhP3xy12aEl7EIGVAVGUgzXiIV/yyAb3V1h4hc1T0+OPlQCI8bEDdimSnaGBeLimnyVliKL5WHkv9q6MVkNssVGwU0CK7cKbGJ4WTrXJegFV3Byn9eGvXqL9xZCu0hjt9ypNlMPrmEawjNQhgmAdOVYXCjmzk5vt7H6g9MUMJRhRUiA==)
      trust-store: 'classpath:security/https/truststore.jks'
      trust-store-password: ENC(dlRt/8qMm5xalAzC4WtYcuuyNhN0TjtxSIRsmJpytK44wSvHNyCPu1Wi14OIa6RS31wtlMaMe5qw7pjPZHXlblAQ0iMRYNA9IfpVJ71JGvdQQx9KS7khdphOzUQJVr1LXr2t/qJ0U6UXZDmOeNtemS65LFWzRZgwiOp4+rHT0S+MmxDJzLQtfkqmB7q7C2i9Im+BwjmVTBBSu/U6F8qwWPj3tJmHb8ONRaQDfMLwxWpEk0kNqVOxhxQiyYE9vVfzAoP10dBxAuJ7aL7yETz3h1WETze22rFB/2ozQLTRWPYcwSP9Xah/p/AAw94M9o0b0xlGfhQhemvk2rfEUbr65w==)
    ```
- Run decrypt goal:

  ```shell
  mvn jasypt:decrypt -Djasypt.encryptor.private-key-location="file:{path_to_private_key}"  -Djasypt.encryptor.private-key-format="PEM" -Djasypt.plugin.path="file:src/main/resources/application.yml"
  ```

  Example:

    ```shell
    mvn jasypt:decrypt -Djasypt.encryptor.private-key-location="file:src/main/resources/security/private.pem"  -Djasypt.encryptor.private-key-format="PEM" -Djasypt.plugin.path="file:src/main/resources/application.yml"
    ```
- Check decrypted values in console output:
    ```yaml
  ssl:
      keyStore: 'classpath:security/https/server.jks'
      key-store-password: DEC(server)
      key-password: DEC(server)
      trust-store: 'classpath:security/https/truststore.jks'
      trust-store-password: DEC(truststore)
    ```

### 1.c - Set decryption key when starting app
Set jasypt encryptor privateKey location and format in application.yml before starting spring boot app.
Example:
```yaml
jasypt:
    encryptor:
        private-key-format: pem
        private-key-location: classpath:/security/private.pem
```

## 2 - Encrypt / decrypt a single value:
### 2.a - Encrypt:
```shell
mvn jasypt:encrypt-value -Djasypt.encryptor.public-key-location="file:src/main/resources/security/public.pem"  -Djasypt.encryptor.public-key-format="PEM" -Djasypt.plugin.value="theValueYouWantToEncrypt"
```

### 2.b - Decrypt:
```shell
mvn jasypt:decrypt-value -Djasypt.encryptor.private-key-location="file:src/main/resources/security/private.pem"  -Djasypt.encryptor.private-key-format="PEM" -Djasypt.plugin.value="ENC(CqqSnvcX5BYoWA5/uF7pfacYVgH8BKUiEFbeaDUAQWjjE8977fiEfWOw9/FnxGSR04sm8WpQ31YsRO0MQ0D18mxqgcWEoCxjNyqR5dyE0+5Yrls+4REpDNSmYT7h2f+LVnKntGNe2ygIqHK1RMQkjX0UN4WgsUn+FtCaSVqmOc8vVv9JoqZsTVIRHrM1oMa0xyLUKhfsRB6QQNx+DLS/emfb5r9H8tTo0WXmVca17Nrdc3Q3/nvcW9V2B6Y+sM3bJl/LxEYoyJ+5oCZc7XOAguSz0/m5iJUTrsZ2VVJj739zhnIB41eDYfw4lvNcxx6Pv0PxSZwMRlv/dwt7hXl07A==)"
```

## 3- Hashing
When using in memory authentication ([`mem-auth`](https://github.com/chutney-testing/chutney/blob/master/packaging/local-dev/src/main/resources/application.yml#:~:text=%2D-,mem%2Dauth,-%2D%20dev%2Dauth)), we need to hide passwords by hashing them.

Hash is calculated using [Bcrypt](https://en.wikipedia.org/wiki/Bcrypt).
### 3.a-  generate bcrypt hash using spring boot cli
- install [spring boot cli](https://docs.spring.io/spring-boot/docs/current/reference/html/getting-started.html#getting-started.installing.cli)
- run:
    ```shell
    spring encodepassword mypassword
    ```
- result:
  ```
  {bcrypt}$2a$10$2Sx9iR2YuayvuIkV6BltQeogIQpd5HCu/.6svECm0CRnlasruIXMe
  ```
- copy/paste the hash to config file. No need to copy `{bcrypt}` string  because BCryptPasswordEncoder is the only configured password encoder in the app.
