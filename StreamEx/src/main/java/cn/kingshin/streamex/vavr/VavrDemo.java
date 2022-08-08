package cn.kingshin.streamex.vavr;


import io.vavr.collection.List;

public class VavrDemo {
    public static void main(String[] args) {
        List<String> list = List.of( "Java", "PHP", "Jquery", "JavaScript", "JShell", "JAVA");
        List<String> lists = List.of( "Java", "PHP", "Jquery", "JavaScript", "JShell", "JAVA");
        List<String> lists1 = List.of( "Java", "PHP", "Jquery", "JavaScript", "JShell", "JAVA");
        List<String> lists2 = List.of( "Java", "PHP", "Jquery", "JavaScript", "JShell", "JAVA");
        List<String> lists3 = List.of( "Java", "PHP", "Jquery", "JavaScript", "JShell", "JAVA");

        //使用drop()及其变体来删除前N个元素：
        List list1 = list.drop(2);//从列表中的第一个元素开始删除n个元素
        System.out.println((list1.contains("java") && list1.contains("PHP")));

        List list2 = lists.dropRight(2);//从列表中的最后一个元素开始删除2个元素
        System.out.println(list2);


        List list3 = lists1.dropUntil(s -> s.contains("Shell"));//从列表中删除元素，直到条件评估为真
        System.out.println("list3"+list3);

        List list4 = lists2.dropWhile(s -> s.length() > 0);//在条件为真时继续删除元素
        System.out.println(list4);

        System.out.println(lists3.take(1));//从列表正序获取第n个元素
        System.out.println("takeRight:"+lists3.takeRight(1));//从列表倒序开始获取元素
        List<String> until = lists3.takeUntil(s -> s.length() > 6);//从列表中获取元素，直到条件为真。
        System.out.println(until);

    }




}
