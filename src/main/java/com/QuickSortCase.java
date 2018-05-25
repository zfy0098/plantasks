package com;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created with IDEA by Zhoufy on 2018/4/25.
 *
 * @author Zhoufy
 */
public class QuickSortCase {


    public static void main(String[] args){
        ThreadLocalRandom random = ThreadLocalRandom.current();

        int count = 20;

        Integer[] number = new Integer[count];
        for (int i = 0; i < count; i++) {
            number[i] = random.nextInt(20 , 200);
        }

        System.out.println(Arrays.toString(number));
        quickSort(number , 0 , count-1);
        System.out.println(Arrays.toString(number));


    }

    public static void quickSort(Integer[] number , int start , int end){
        if(start < end){
            int i = start , j = end , tem , base = number[start];

            do {
                while (i < end && base > number[i]){i++;}
                while (j > start && base < number[j]){j--;}
                if (i <= j ){
                    tem = number[i];
                    number[i] = number[j];
                    number[j] = tem;
                    i++;
                    j--;
                }
            }while (i < j);
            if (i < end){quickSort(number , i , end);}
            if (j > start){quickSort(number , start , j);}
        }
    }
}
