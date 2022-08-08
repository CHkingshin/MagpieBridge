StreamEX这个模块是一个基于包括像：guava commons、vavr、hutool来达到代码简洁统一的Demo
便于理解MagpieBridge里我写得很奇怪的代码

函数式、流式编程、泛型 vavr Try.of Tule Case
streamex默认实现default 方法 Pair

hutool对于集合的判断、Assert断言

2
防御性编程
Optional 用于查询的时候的返回类型 方便下一步操作的时候强制进行判断是否为空 
Iterables
Objiects.IsNull
IterUtil
MapUtils
StringUtils
Assert

3
对象模式的封装 
模板 抽象类
builder 
step builder 
pipeline 
门面 也就是防腐层（直接根防腐层打交道，不必关系细节是怎样实现的）单体应用时候的远程调用的

4
代码生成隐藏细节 starter 隐藏细节（如果你只用不必深究这一块）
mapstruct
querydsl 
lombok自定义注解 
Converter Mapper

Spi Enablexx注入

