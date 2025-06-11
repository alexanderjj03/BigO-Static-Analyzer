package testJavaFiles;

import java.util.Arrays;

public class Example {
    int poof;

    public void selectionSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[minIndex]) {
                    minIndex = j;
                }
            }
            int temp = arr[minIndex];
            arr[minIndex] = arr[i];
            arr[i] = temp;
        }
    }

    public void whileLoopTest(int n, int m, int[] arr) {
        int j = n + m;
        int i = 0;
        while (i < j) {
            i = i * 2;
        }
        for (int k = 0; k < j; k++) {
            System.out.println(k);
        }
        Arrays.sort(arr);
    }

    public static void main(String[] args) {
        Example example = new Example();
        int[] numbers = {5, 3, 8, 4, 2};
        example.selectionSort(numbers);
        for (int num : numbers) {
            System.out.print(num + " ");
        }
    }
}