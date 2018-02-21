function PeachUtils() {
    this.numHold = -1;

    this.binarySearch = function (array, key) {
        this.numHold = -1;
        var lo = 0,
            hi = array.length - 1,
            mid,
            element;
        while (lo <= hi) {
            mid = Math.floor((lo + hi) / 2, 10);
            element = array[mid].toUpperCase();
            key = key.toUpperCase();

            if (element.startsWith(key) && this.numHold < 0) {
                this.numHold = mid;
            }
            if (element < key) {
                lo = mid + 1;
            } else if (element > key) {
                hi = mid - 1;
            } else {
                return mid;
            }
        }
        return -1;
    }
}

