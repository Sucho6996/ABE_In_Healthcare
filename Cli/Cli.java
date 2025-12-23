import java.util.Scanner;

public class Cli {
    public static void main(String[] args) {
        Scanner sc=new Scanner(System.in);
        while (true){
            System.out.println("\n=== ABE Service CLI ===");
            System.out.println("You want to login as: ");
            System.out.println("1.User\n2.Doctor\n3.Hospital\n4.Attribute Authority\n5.Exit");
            System.out.print("Your choice: ");
            String choice=sc.nextLine();
            if(choice.contains("1")) UserCli.main(args);
            else if(choice.contains("2")) DoctorCli.main(args);
            else if(choice.contains("3")) HospitalCli.main(args);
            else if(choice.contains("4")) AtributeAuth.main(args);
            else if(choice.contains("5")) {
                System.out.println("Exiting CLI...");
                return;
            }
            else System.out.println("Invalid choices!!!");
        }
    }
}
