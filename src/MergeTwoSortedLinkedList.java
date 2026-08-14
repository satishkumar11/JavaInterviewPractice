

class Node {
    int value;
    Node next;
    Node(int value) {
        this.value = value;
    }
}

public class MergeTwoSortedLinkedList {


    public static void main(String[] args) {
        Node head1 = new Node(5);
        head1.next = new Node(10);
        head1.next.next = new Node(15);
        head1.next.next.next = new Node(40);

        Node head2 = new Node(2);
        head2.next = new Node(3);
        head2.next.next = new Node(20);

        Node res = sortedMerge(head1, head2);

        while(res!= null) {
            System.out.println(res.value);
            res = res.next;
        }
    }


    static Node sortedMerge(Node head1, Node head2) {
        Node result = new Node(-1);
        Node current = result;

        while (head1 != null && head2 != null) {
            if(head1.value <= head2.value) {
                current.next = head1;
                head1 = head1.next;
            } else {
                current.next = head2;
                head2 = head2.next;
            }
            current = current.next;
        }


        current.next = head1 != null ? head1 : head2;

        return result.next;
    }
}

