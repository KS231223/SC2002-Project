package common;

import exceptions.*;

import java.util.Scanner;

public abstract class Controller {
    public Scanner scanner;
    public Router router;
    public Controller(Router router,Scanner scanner){
        this.router = router;
        this.scanner = scanner;
    }
    public abstract void initialize();
    //

}
