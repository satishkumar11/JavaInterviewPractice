package factorypattern;

// FACTORY PATTERN — A helper that creates objects for you, so you don't write
// "new" everywhere. You just say "give me a Circle" and it hands you one.

// 1. Product interface — a contract that every shape must follow
interface Shape {
    void draw();
}

// 2. Concrete products — each is a different kind of shape
class Circle implements Shape {
    public void draw() {
        System.out.println("Drawing a Circle.");
    }
}

class Square implements Shape {
    public void draw() {
        System.out.println("Drawing a Square.");
    }
}

class Rectangle implements Shape {
    public void draw() {
        System.out.println("Drawing a Rectangle.");
    }
}

// 3. Factory — decides which concrete shape to create, based on input
class ShapeFactory {
    public Shape getShape(String type) {
        if (type.equalsIgnoreCase("CIRCLE")) {
            return new Circle();
        } else if (type.equalsIgnoreCase("SQUARE")) {
            return new Square();
        } else if (type.equalsIgnoreCase("RECTANGLE")) {
            return new Rectangle();
        }
        return null; // unknown type
    }
}

// Main class to test it
public class FactoryPatternDemo {
    public static void main(String[] args) {
        ShapeFactory shapeFactory = new ShapeFactory();

        Shape shape1 = shapeFactory.getShape("CIRCLE");
        shape1.draw(); // Drawing a Circle.

        Shape shape2 = shapeFactory.getShape("SQUARE");
        shape2.draw(); // Drawing a Square.

        Shape shape3 = shapeFactory.getShape("RECTANGLE");
        shape3.draw(); // Drawing a Rectangle.
    }
}
