package ims;

import common.Router;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Internship Placement Management System...\n");
        Router router = new Router();
        Scanner scanner = new Scanner(System.in);
        Authentication authentication = new Authentication(router, scanner);
        authentication.start();

    }
}


