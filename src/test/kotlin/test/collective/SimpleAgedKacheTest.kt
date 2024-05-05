class CustomLinkedList<T> {
    private var head: Node<T>? = null
    private var tail: Node<T>? = null
    var size = 0
        private set

    fun add(value: T) {
        val node = Node(value)
        if (isEmpty()) {
            head = node
        } else {
            tail?.next = node
        }
        tail = node
        size++
    }

    fun remove(value: T) {
        var prev: Node<T>? = null
        var curr = head
        while (curr != null && curr.value != value) {
            prev = curr
            curr = curr.next
        }
        if (prev == null) {
            head = curr?.next
        } else if (curr != null) {
            prev.next = curr.next
            if (curr.next == null) {
                tail = prev
            }
        }
        if (curr != null) {
            size--
        }
    }

    fun find(predicate: (T) -> Boolean): T? {
        var curr = head
        while (curr != null) {
            if (predicate(curr.value)) {
                return curr.value
            }
            curr = curr.next
        }
        return null
    }

    fun isEmpty(): Boolean {
        return size == 0
    }

    inner class Node<T>(var value: T, var next: Node<T>? = null)
}
