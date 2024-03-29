package cn.chihsien.constant;

/**
 * <h1>授权需要使用的一些常量信息</h1>
 *
 * @author KingShin
 */
public final class AuthorityConstant {

    /**
     * RSA 私钥, 除了授权中心以外, 不暴露给任何客户端
     */
    public static final String PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCOW/udnf3mtpRbP9LbcfhzG5ebFqQdYred1xtVw5NoFUoK7U5HSi6qnuoUQr6H/dtCUIVJgu9ATkLlcRQXmepzZFVLjOMP1+xClUmgZrKs4MapMC4iA/Q4TD/p5fQpPLHgWW3B0Tq/D0hjUMYmE1TWJjro3LekKk1Bgn8xmYr5LjPdCV98q54vEHIc5Td2bXCDE2G+LQGA52DVdNgxKGHIs/D/8pGwqjWohVFXNl384x3IBps9CzQg8QTYKdPB9XQ29xohWw62XHp2dO8Ou1TsQms2P2F0CxuoLTl19PbFlTwC7vohiWpD86FJpSljpcPtm0rNA0vsTWFiU4970FPbAgMBAAECggEAbLkLXcFAHGfsvhPscfSFD0RPcP7FsDuoiD+0fLe1aJR5zAaY9hFNurlLhNXEtg/G2MquQQpitvYOWTUXZz8Bs8c47U5YS81YfzGM3ZgRdb7leml1kjz609RIK9aXtrbIEtO544BsIWcGnfMeog25iSQBcb6/8fzsq8+CXNsrLEpdcKuBaWPAsNqt7GFa7SuCobD5ejufYXKXiDEmNQ/d/WcbjVeK4g2FJBEEZyoZ+JawOFUDTe5XevC+JtWZvcmXsnxNKjvZxmq1pQsHFjBpcvQtRRaBkrjFCMroL0rwTTWPtFU4TzuSiffk7jY56j8ZOlNSUa5VUScj8MGOeqtIIQKBgQC+/dhrZdkxYxv0uMOBAAsOWvkGuTLognpI//WzNMHgz5vGKZThlz38y06M7T8KYImQz7yoviTYKyyIQHrI/8qjIk02bhfU/6Xx0O4prHiNcnnyTG4CNTODNu56Y9LYzSGaEP36FMuXUGEB2AM0nUAh9TfMeh1FJc2r5/dGLDawbQKBgQC+0Im2le0ScqfrR52s+iV/0M6+jiBol+B+6UbZqoOUm14MqJBCvUEO76LeZUSs279+Cj7MkhTqgoW79JFQA63zFSzTur7LzEDqkDR8QhT0GXSCMfTatSSadbSVZ9bfOQ0ByNXJbDPXYhQM4yRWxRUcHzyo1PjFk3jzsYeu6GW4ZwKBgFigHGwBr95N+iv+DKJwrbC0oDfxemEQIsR7Gb1Vrt2uGL/EjdR8xQPoJ4QXlxguocczznyS9y/kIN785L0ejR5UDXZDcgWht5RVeh6WbL5eL54yvx2BKG0r8U+TwMRfs8b1OZVxTuPhJZKgdSRWD+tyCbl1tN1gZGbv1fuXCq2lAoGBALYYMLPL09f9Dv5a87/1X//f9symuYrXFWhHmlNyg+s3ccNGwZTPItzsF1OA9M65Tr8ra86xwcAiGxnJRTtEp6YImSTdW+ME4xZ104CZV4GzIRp2LiLhJzM4DMNfuxB/U0hWrp5v4fEzKcs6oJ0lqwWRUcQUqety1sRIsSFbqmcVAoGAc5xqTKJHO0MN8o2JGKDX5RQ1Kt0xvSlzioI8vXhrJMpiEIc6HHtkGtInJj73xfW8cyiTUmkRVzWXVt0PhV0MBbROEF8Xc+GLriuNROSMfO2nG8rPAthEy8GF+XjOt17+chb46f4dwymLc+DY36TVwTm7bZ9l0vhOryZac9cdSUc=";


    /**
     * 默认的 Token 超时时间, 一天
     */
    public static final Integer DEFAULT_EXPIRE_DAY = 1;
}
