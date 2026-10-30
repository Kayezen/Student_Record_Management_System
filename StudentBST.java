import java.util.ArrayList;
import java.util.List;

public class StudentBST {

    // ---- Node ----
    static class Node {
        int rollNumber;
        String name;
        String grade;
        Node left, right;

        Node(int rollNumber, String name, String grade) {
            this.rollNumber = rollNumber;
            this.name = name;
            this.grade = grade;
        }
    }

    private Node root;

    // gives outside classes (like the tree visualizer) read access to the tree
    public Node getRoot() {
        return root;
    }

    // ---- Insert ----
    public void insert(int rollNumber, String name, String grade) {
        root = insertRec(root, rollNumber, name, grade);
    }

    private Node insertRec(Node node, int rollNumber, String name, String grade) {
        if (node == null) {
            return new Node(rollNumber, name, grade);
        }
        if (rollNumber < node.rollNumber) {
            node.left = insertRec(node.left, rollNumber, name, grade);
        } else if (rollNumber > node.rollNumber) {
            node.right = insertRec(node.right, rollNumber, name, grade);
        } else {
            // duplicate roll number: update existing record instead of inserting twice
            node.name = name;
            node.grade = grade;
        }
        return node;
    }

    public boolean rollNumberExists(int rollNumber) {
        return searchByRoll(rollNumber) != null;
    }

    // ---- Search by roll number (BST key) ----
    public Node searchByRoll(int rollNumber) {
        return searchRollRec(root, rollNumber);
    }

    private Node searchRollRec(Node node, int rollNumber) {
        if (node == null || node.rollNumber == rollNumber) {
            return node;
        }
        if (rollNumber < node.rollNumber) {
            return searchRollRec(node.left, rollNumber);
        }
        return searchRollRec(node.right, rollNumber);
    }

    // ---- Search by name (not the key, so it's a full scan) ----
    public List<Node> searchByName(String name) {
        List<Node> results = new ArrayList<>();
        searchNameRec(root, name.toLowerCase(), results);
        return results;
    }

    private void searchNameRec(Node node, String lowerName, List<Node> results) {
        if (node == null) return;
        searchNameRec(node.left, lowerName, results);
        if (node.name.toLowerCase().contains(lowerName)) {
            results.add(node);
        }
        searchNameRec(node.right, lowerName, results);
    }

    // ---- Delete by roll number ----
    public void deleteByRoll(int rollNumber) {
        root = deleteRec(root, rollNumber);
    }

    private Node deleteRec(Node node, int rollNumber) {
        if (node == null) return null;

        if (rollNumber < node.rollNumber) {
            node.left = deleteRec(node.left, rollNumber);
        } else if (rollNumber > node.rollNumber) {
            node.right = deleteRec(node.right, rollNumber);
        } else {
            // found the node to delete
            if (node.left == null && node.right == null) {
                return null; // no children
            }
            if (node.left == null) {
                return node.right; // one child (right)
            }
            if (node.right == null) {
                return node.left; // one child (left)
            }
            // two children: replace with inorder successor (smallest in right subtree)
            Node successor = findMin(node.right);
            node.rollNumber = successor.rollNumber;
            node.name = successor.name;
            node.grade = successor.grade;
            node.right = deleteRec(node.right, successor.rollNumber);
        }
        return node;
    }

    private Node findMin(Node node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    // ---- Traversals ----
    public List<Node> inorder() {
        List<Node> result = new ArrayList<>();
        inorderRec(root, result);
        return result;
    }

    private void inorderRec(Node node, List<Node> result) {
        if (node == null) return;
        inorderRec(node.left, result);
        result.add(node);
        inorderRec(node.right, result);
    }

    public List<Node> preorder() {
        List<Node> result = new ArrayList<>();
        preorderRec(root, result);
        return result;
    }

    private void preorderRec(Node node, List<Node> result) {
        if (node == null) return;
        result.add(node);
        preorderRec(node.left, result);
        preorderRec(node.right, result);
    }

    public List<Node> postorder() {
        List<Node> result = new ArrayList<>();
        postorderRec(root, result);
        return result;
    }

    private void postorderRec(Node node, List<Node> result) {
        if (node == null) return;
        postorderRec(node.left, result);
        postorderRec(node.right, result);
        result.add(node);
    }

    public boolean isEmpty() {
        return root == null;
    }

    public int size() {
        return inorder().size();
    }

    public void clear() {
        root = null;
    }
}
