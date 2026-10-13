package ru.learning.taskmanager.console;

import java.util.Objects;
import java.util.Scanner;

public class ConsoleInput {

    private final Scanner scanner;

    public ConsoleInput(Scanner scanner){
        this.scanner = Objects.requireNonNull(
                scanner,
                "Сканер не может быть null"
        );
    }

    public String readRequiredLine(String prompt){
        while(true){
            System.out.println(prompt);

            String input = scanner.nextLine().strip();

            if(!input.isBlank()){
                return input;
            }

            System.out.println("Значение не должно быть пустым");
        }
    }

    public int readInt(String prompt, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException(
                    "Нижняя граница не должна превышать верхнюю"
            );
        }

        while (true) {
            String input = readRequiredLine(prompt);

            try {
                int number = Integer.parseInt(input);

                if (number >= min && number <= max) {
                    return number;
                }

                System.out.println(
                        "Введите число от " + min + " до " + max + "."
                );
            } catch (NumberFormatException exception) {
                System.out.println("Введите целое число.");
            }
        }
    }

    public long readPositiveLong(String prompt){
        while(true){
            String input = readRequiredLine(prompt);

            try{
                long number = Long.parseLong(input);

                if (number > 0){
                    return number;
                }

                System.out.println("Number должно быть больше нуля");
            }catch (NumberFormatException exception){
                System.out.println(
                        "Введите целое число в пределах диапазона long"
                );
            }
        }
    }
}
