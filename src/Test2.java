import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test2 {
    public static void main(String[] args) {
        String str = "22244444477777777222222666666";
        System.out.println(keypad(str));
    }

    public static String keypad(String str)
    {
        Map<Character, char[]> map = new HashMap<>();

        map.put('1',new char[]{',','@'});
        map.put('2',new char[]{'A','B','C','a','b','c','2'});
        map.put('3',new char[]{'D','E','F','d','e','f','3'});
        map.put('4',new char[]{'G','H','I','g','h','i','4'});
        map.put('5',new char[]{'J','K','L','j','k','l','5'});
        map.put('6',new char[]{'M','N','O','m','n','o','6'});
        map.put('7',new char[]{'P','Q','R','S','p','q','r','s','7'});
        map.put('8',new char[]{'T','U','V','t','u','v','8'});
        map.put('9',new char[]{'W','X','Y','w','x','y','z','9'});
        map.put('0',new char[]{' ','0'});

        StringBuffer result = new StringBuffer();
        for(int i = 0 ; i < str.length();i++)
        {
            char temp = str.charAt(i);
            int index = i;
            int tempIndex = -1;
            char [] innerList = map.get(temp);
            while(index < str.length() && (str.charAt(index)!='_' && str.charAt(index)==temp))
            {
                System.out.println(tempIndex);
                index++;
                tempIndex++;
            }
            tempIndex = tempIndex%(map.get(temp).length);
            result.append(innerList[tempIndex]);
            System.out.println(result);
            if(index < str.length() && str.charAt(index)!='_')
                i = index-1;
            else
                i = index;
        }

        return result.toString();

    }

}
