package util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** A Set2D implemented with a QuadTree.  The type argument, Point,
 *  indicates the type of points contained in the set.  The rather
 *  involved type parameter structure here allows you to extend
 *  QuadPoint, and thus add additional data and methods to the points
 *  you store.
 *  @author Daniel Paden Tomasello cs61b-bz
 */
public class QuadTree<Point> extends Set2D<Point> {

    /** An empty set of Points that uses VIEW to extract position information.
     *  The argument TRANSITIONSIZE has no externally
     *  visible effect, but may affect performance.  It is intended to specify
     *  the largest set Points that resides unsubdivided in a single node of
     *  the tree.  While space-efficient, such nodes have slower search times
     *  as their size increases.
     *  */
    public QuadTree(PointView<Point> view, int transitionSize) {
        super(view);
        _transitionSize = transitionSize;
        _root = new InternalNode<Point>(getView(), _transitionSize);
    }

    /* PUBLIC METHODS.  See Set2D.java for documentation */

    /** Returns my transitionSize parameter (supplied to my constructor). */
    public int getTransitionSize() {
        return _transitionSize;
    }

    @Override
    public int size() {
        return _root.size();
    }

    @Override
    public void add(Point p) {
        _root.add(p);
    }

    @Override
    public void remove(Point p) {
        _root.remove(p);
    }

    @Override
    public boolean contains(Point p) {
        return _root.contains(p);
    }

    @Override
    public Iterator<Point> iterator() {
        return _root.iterator();
    }

    @Override
    public Iterator<Point> iterator(double xl, double yl,
            double xu, double yu) {
        return _root.iterator(xl, yl, xu, yu);
    }


    /* END OF PUBLIC MEMBERS */

    /** The maximum size for an unsubdivided node. */
    private int _transitionSize;

    /** The root of the tree. */
    private InternalNode<Point> _root = null;

    /** Class for tree nodes. */
    private abstract class TreeNode<A> {
        /** PointView for TreeNode. */
        protected final PointView<A> _view;
        /** Transition Size. */
        protected final int _transitionSize;

        /** Constructor for any TreeNode. Sets _view to VIEW and
         * _transitionSize to TRANSITIONSIZE
         */
        private TreeNode(PointView<A> view, int transitionSize) {
            _view = view;
            _transitionSize = transitionSize;
        }
        /** Add POINT to me. */
        public abstract void add(A point);
        /** Returns true if I am a leaf. */
        public abstract boolean isLeaf();
        /** Returns size of tree starting at me. */
        public abstract int size();
        /** Returns Iterator through me. */
        public abstract Iterator<A> iterator();
        /** Returns true if I contain POINT. */
        public abstract boolean contains(A point);
        /** Removes POINT from me if it exists. */
        public abstract void remove(A point);
        /** Returns Iterator only in region XL, YL, XU, YU. */
        public abstract Iterator<A> iterator(double xl
                , double yl, double xu, double yu);
    }
    /** Class that represents Internal Node of Tree. */
    private class InternalNode<A> extends TreeNode<A> {
        /** Northwest node. */
        private TreeNode<A> nwnode;
        /** North East node. */
        private TreeNode<A> nenode;
        /** South West node. */
        private TreeNode<A> swnode;
        /** South East node. */
        private TreeNode<A> senode;
        /** Root. */
        private A root;

        /** Sets viewer to VIEW and transition size to TRANSITIONSIZE. */
        private InternalNode(PointView<A> view, int transitionSize) {
            super(view, transitionSize);
            nwnode = new LeafNode<A>(view, transitionSize);
            nenode = new LeafNode<A>(view, transitionSize);
            swnode = new LeafNode<A>(view, transitionSize);
            senode = new LeafNode<A>(view, transitionSize);
        }

        /** Returns size of root. */
        public int size() {
            if (root == null) {
                return 0;
            } else {
                return 1 + nwnode.size()
                        + nenode.size() + swnode.size() + senode.size();
            }
        }
        /** Removes POINT from me. */
        public void remove(A point) {
            if (_view.equals(point, root)) {
                Iterator<A> iter = this.iterator();
                iter.next();
                if (iter.hasNext()) {
                    root = iter.next();
                } else {
                    root = null;
                }
                nwnode = new LeafNode<A>(_view, _transitionSize);
                nenode = new LeafNode<A>(_view, _transitionSize);
                swnode = new LeafNode<A>(_view, _transitionSize);
                senode = new LeafNode<A>(_view, _transitionSize);
                while (iter.hasNext()) {
                    add(iter.next());
                }
            } else {
                double xpoint = _view.getX(point);
                double ypoint = _view.getY(point);
                double xRoot = _view.getX(root);
                double yRoot = _view.getY(root);
                int quadrant = quadrant(xpoint, ypoint, xRoot, yRoot);
                switch(quadrant) {
                case 2:
                    nwnode.remove(point);
                    break;
                case 0:
                    nenode.remove(point);
                    break;
                case 1:
                    nenode.remove(point);
                    break;
                case 3:
                    swnode.remove(point);
                    break;
                case 4:
                    senode.remove(point);
                    break;
                default:
                    break;
                }
            }
        }

        /** Returns true if I contain POINT. */
        public boolean contains(A point) {
            if (root == null) {
                return false;
            }
            if (_view.equals(point, root)) {
                return true;
            } else {
                double xpoint = _view.getX(point);
                double ypoint = _view.getY(point);
                double xRoot = _view.getX(root);
                double yRoot = _view.getY(root);
                int quadrant = quadrant(xpoint, ypoint, xRoot, yRoot);
                switch(quadrant) {
                case 1:
                    return nenode.contains(point);
                case 0:
                    return nenode.contains(point);
                case 2:
                    return nwnode.contains(point);
                case 3:
                    return swnode.contains(point);
                case 4:
                    return senode.contains(point);
                default:
                    return false;
                }
            }
        }

        /** Returns Iterator of me. */
        public Iterator<A> iterator() {
            return new InternalNodeIterator();
        }

        /** Returns Iterator over region XL, YL, XU, YU. */
        public Iterator<A> iterator(double xl, double yl,
                double xu, double yu) {
            return new BBoxIterator(xl, yl, xu, yu);
        }
        /** An iterator through an InternalNode. */
        private class InternalNodeIterator implements Iterator<A> {
            /** Next value of Iterator. */
            private A next;
            /** Previous value that is returned. */
            private A result;
            /** NorthEast node Iterator. */
            private Iterator<A> neiter;
            /** NorthWest node Iterator. */
            private Iterator<A> nwiter;
            /** SouthWest node Iterator. */
            private Iterator<A> switer;
            /** SouthEast Iterator. */
            private Iterator<A> seiter;
            /** Constructor for InternalIterator. */
            public InternalNodeIterator() {
                next = root;
                neiter = nwnode.iterator();
                nwiter = nenode.iterator();
                switer = swnode.iterator();
                seiter = senode.iterator();
            }
            /** Returns true if I have next. */
            public boolean hasNext() {
                return (next != null);
            }
            /** returns next value in me. */
            public A next() {
                result = next;
                if (neiter.hasNext()) {
                    next = neiter.next();
                } else if (nwiter.hasNext()) {
                    next = nwiter.next();
                } else if (switer.hasNext()) {
                    next = switer.next();
                } else if (seiter.hasNext()) {
                    next = seiter.next();
                } else {
                    next = null;
                }
                if (result == null) {
                    throw new NoSuchElementException();
                }
                return result;
            }
            /** Does nothing in this implementation. */
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        /** An iterator over bounded point. */
        private class BBoxIterator implements Iterator<A> {
            /** Underlying iterator over tree. */
            private Iterator<A> _iter;

            /** A new iterator over the bounding box (XL,YL) to (XU,YU). */
            public BBoxIterator(double xl, double yl, double xu, double yu) {
                _xl = xl;
                _yl = yl;
                _xu = xu;
                _yu = yu;
                _next = null;
                _iter = selectSmallest(xl, yl, xu, yu);
            }

            @Override
            public boolean hasNext() {
                while (_next == null && _iter.hasNext()) {
                    _next = _iter.next();
                    if (!Set2D.isWithin(_view.getX(_next),
                            _view.getY(_next), _xl, _yl, _xu, _yu)) {
                        _next = null;
                    }
                }
                return _next != null;
            }

            @Override
            public A next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                A v = _next;
                _next = null;
                return v;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            /** Bounds of my bounding box. */
            private double _xl, _yl, _xu, _yu;
            /** Next point to deliver, or null if not yet fetched from
             *  _points. */
            private A _next;

            /** Select Node. Use bound XL, YL, XU, YU and
             * Returns smallest node iterator. */
            private Iterator<A> selectSmallest(
                    double xl, double yl, double xu, double yu) {
                if (root == null) {
                    return iterator();
                }
                double xRoot = _view.getX(root);
                double yRoot = _view.getY(root);
                int quadl = quadrant(xl, yl, xRoot, yRoot);
                int quadu = quadrant(xu, yu, xRoot, yRoot);
                if ((quadl == 0 & quadu == 2) | (quadl == 2 & quadu == 0)) {
                    return nenode.iterator();
                }
                if (quadl != quadu) {
                    return iterator();
                } else if (quadl == 2) {
                    return nwnode.iterator(_xl, _yl, _xu, _yu);
                } else if (quadl == 1 | quadl == 0) {
                    return nenode.iterator(_xl, _yl, _xu, _yu);
                } else if (quadl == 3) {
                    return swnode.iterator(_xl, _yl, _xu, _yu);
                } else {
                    return senode.iterator(_xl, _yl, _xu, _yu);
                }

            }
        }

        /** Returns true if LeafNode. */
        public boolean isLeaf() {
            return false;
        }
        /** Adds POINT to selected branch based on location. */
        public void add(A point) {
            if (root == null) {
                root = point;
            } else if (!contains(point)) {
                double xpoint = _view.getX(point);
                double ypoint = _view.getY(point);
                double xRoot = _view.getX(root);
                double yRoot = _view.getY(root);
                int quadrant = quadrant(xpoint, ypoint, xRoot, yRoot);
                switch(quadrant) {
                case 2:
                    nwnode = addToNode(nwnode, point);
                    break;
                case 0:
                    nenode = addToNode(nenode, point);
                    break;
                case 1:
                    nenode = addToNode(nenode, point);
                    break;
                case 3:
                    swnode = addToNode(swnode, point);
                    break;
                case 4:
                    senode = addToNode(senode, point);
                    break;

                default:
                    break;
                }
            }
        }
        /** Adds POINT TO NODE and returns the new node. */
        private TreeNode<A> addToNode(TreeNode<A> node, A point) {
            if (node.isLeaf()) {
                if (((LeafNode<A>) node).isFull()) {
                    InternalNode<A> temp =
                            new InternalNode<A>(_view, _transitionSize);
                    for (int i = 0;
                            i < node.size();
                            i += 1) {
                        temp.add(((LeafNode<A>) node).child(i));
                    }
                    temp.add(point);
                    node = temp;

                } else {
                    node.add(point);
                }
            } else {
                node.add(point);
            }
            return node;
        }
    }
    /** LeafNode Object. */
    private class LeafNode<A> extends TreeNode<A> {
        /** ArrayList holding Points. */
        private ArrayList<A> items = new ArrayList<A>();
        /** Constructor which sets _view to VIEW and _transitionSize
         * to TRANSITIONSIZE.
         */
        private LeafNode(PointView<A> view, int transitionSize) {
            super(view, transitionSize);
        }
        /** Add Point P to LeafNode. */
        public void add(A p) {
            items.add(p);
        }
        /** Returns true if I contain POINT. */
        public boolean contains(A point) {
            return items.contains(point);
        }

        /** Returns number of items in Leaf. */
        public int size() {
            return items.size();
        }

        /** Returns item number K of items. */
        private A child(int k) {
            return items.get(k);
        }

        /** Returns true if LeadNode. */
        public boolean isLeaf() {
            return true;
        }

        /** Returns true if leaf contains max number of items. */
        private boolean isFull() {
            return items.size() == _transitionSize;
        }
        /** Returns Iterator of me. */
        public Iterator<A> iterator() {
            return items.iterator();
        }
        /** Returns iterator of node with bounds X Y Z T.
         * . (Bounds Not relevant if leaf) */
        public Iterator<A> iterator(double x, double y, double z, double t) {
            return items.iterator();
        }
        /** Removes POINT from me. */
        public void remove(A point) {
            items.remove(point);
        }
    }
}

