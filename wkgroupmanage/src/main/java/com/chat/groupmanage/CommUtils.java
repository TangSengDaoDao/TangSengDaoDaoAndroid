package com.chat.groupmanage;

import com.chat.groupmanage.entity.GroupMemberEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 2020-10-19 17:56
 */
public class CommUtils {
    private CommUtils() {
    }

    private static class CommUtilsBinder {
        final static CommUtils commonUtils = new CommUtils();
    }

    public static CommUtils getInstance() {
        return CommUtilsBinder.commonUtils;
    }


    /**
     * 判断首字母是否为字母
     *
     * @param str
     * @return
     */
    public boolean isStartLetter(String str) {
        String temp = str.substring(0, 1);
        return Character.isLetter(temp.charAt(0));
    }

    /**
     * 判断首字母是否数字
     *
     * @param str
     * @return
     */
    public boolean isStartNum(String str) {
        String temp = str.substring(0, 1);
        return Character.isDigit(temp.charAt(0));
    }


    public void sortListBasic1(List<GroupMemberEntity> list) {
        transferListBasic1(list);
    }

    /**
     * 进行冒泡排序
     *
     * @param list
     */
    private void transferListBasic1(List<GroupMemberEntity> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = 0; j < list.size() - 1 - i; j++) {
                exchangeNameOrderBasic1(j, list);
            }
        }
    }

    /**
     * 交换两个名字的顺序,根据首字母判断
     *
     * @param j
     * @param list
     */
    private void exchangeNameOrderBasic1(int j, List<GroupMemberEntity> list) {
        String namePinYin1 = list.get(j).pying;
        String namePinYin2 = list.get(j + 1).pying;

        int size = namePinYin1.length() >= namePinYin2.length() ? namePinYin2.length() : namePinYin1.length();
        for (int i = 0; i < size; i++) {
            char jc = namePinYin1.charAt(i);
            char jcNext = namePinYin2.charAt(i);
            if (jc < jcNext) {//A在B之前就不用比较了
                break;
            }
            if (jc > jcNext) {//A在B之后就直接交换,让A在前面B在后面
                GroupMemberEntity nameBean = list.get(j);
                list.set(j, list.get(j + 1));
                list.set(j + 1, nameBean);
                break;
            }
            //如果AB一样就继续比较后面的字母
        }
    }


    public ArrayList<String> getList() {
        ArrayList<String> customLetters = new ArrayList<>();
        customLetters.add("A");
        customLetters.add("B");
        customLetters.add("C");
        customLetters.add("D");
        customLetters.add("E");
        customLetters.add("F");
        customLetters.add("G");
        customLetters.add("H");
        customLetters.add("I");
        customLetters.add("J");
        customLetters.add("K");
        customLetters.add("L");
        customLetters.add("M");
        customLetters.add("N");
        customLetters.add("O");
        customLetters.add("P");
        customLetters.add("Q");
        customLetters.add("R");
        customLetters.add("S");
        customLetters.add("T");
        customLetters.add("U");
        customLetters.add("V");
        customLetters.add("W");
        customLetters.add("X");
        customLetters.add("Y");
        customLetters.add("Z");
        customLetters.add("#");
        return customLetters;
    }
}
