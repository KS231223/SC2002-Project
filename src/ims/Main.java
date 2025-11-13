package ims;

import common.Router;
import java.util.Scanner;

/** Launches the Internship Placement Management System from the command line. */
public class Main {
    /**
     * Configures shared infrastructure (router and scanner) and begins the authentication flow.
     *
     * @param args ignored command-line arguments
     */
    public static void main(String[] args) {
        System.out.println("Starting Internship Placement Management System...\n");
        Router router = new Router();
        Scanner scanner = new Scanner(System.in);
        Authentication authentication = new Authentication(router, scanner);
        authentication.start();

    }
}


