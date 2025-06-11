package testJavaFiles;

public class SelectionSort { // Max time O(n^2)
    int x = 69;
    public void selectionSort(int[] arr) { // time: O(n^2)
        int n = arr.length; // time: O(1)
        yo();
        for (int i = 0; i < n - 1; i++) { // time: O(n^2)
            int minIndex = i; // time: O(1)
            for (int j = i + 1; j < n; j++) { // time: O(n)
                if (arr[j] < arr[minIndex]) { // time: O(1)
                    minIndex = j; // time: O(1)
                }
            }
            int temp = arr[minIndex]; // time: O(1)
            arr[minIndex] = arr[i]; // time: O(1)
            arr[i] = temp; // time: O(1)
        }
    }

    private void yo() {};
}
