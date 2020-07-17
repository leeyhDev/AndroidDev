package com.leeyh.algorithm;

public class 两数相加 {

    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode dummyHead = new ListNode(0);
        ListNode p = l1, q = l2, curr = dummyHead;
        int carry = 0;
        int x;
        int y;
        int sum;
        while (p != null || q != null) {
            x = (p != null) ? p.val : 0;
            y = (q != null) ? q.val : 0;
            sum = carry + x + y;
            carry = sum / 10;
            curr.val = sum % 10;
            if (p != null)
                p = p.next;
            if (q != null)
                q = q.next;
            if (q != null || p != null) {
                curr.next = new ListNode(0);
                curr = curr.next;
            }
        }
        if (carry == 1) {
            curr.next = new ListNode(carry);
        }
        return dummyHead;
    }

    public class ListNode {
        int val;
        ListNode next;

        public ListNode(int x) {
            val = x;
        }
    }
}



