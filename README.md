# SignInput
かんたんに文字を入力できるプラグインです。

## usage

```java
import org.bukkit.entity.Player;
import net.monkeyfunky.devteam.signinput.SignInput;

public class Debug {
    public static void a(Player player) {
        SignInput.input(player, "ここがタイトル", (Player::sendMessage));
    }

    public static void b(Player player) {
        SignInput.input(player, "ここがタイトル", (player, string) -> {
            System.out.println(string);
            TestClass.runSomething(player, string);
        });
    }
}
```

aの実行結果<br>

![image](https://raw.githubusercontent.com/zomzoneproject/SignInput/master/images/1.png)
このように表示される。
![image](https://raw.githubusercontent.com/zomzoneproject/SignInput/master/images/2.png)
文章を入力して完了を押すと
![image](https://raw.githubusercontent.com/zomzoneproject/SignInput/master/images/3.png)
入力した内容がプレイヤーに送信される。


## author
- eight_y_88