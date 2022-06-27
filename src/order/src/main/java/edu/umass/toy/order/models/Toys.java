package edu.umass.toy.order.models;

public enum Toys {
    BARBIE("BARBIE", "15.99", "Barbie"),
    CRAYOLA_CRAYON("CRAYOLACRAYON", "16.99", "Crayola Crayon"),
    ERECTOR_SET("ERECTORSET", "17.99", "Erector Set"),
    ETCH_A_SKETCH("ETCHASKETCH", "18.99", "Etch A Sketch"),
    FRISBEE("FRISBEE", "19.99", "Frisbee"),
    HULA_HOOP("HULAHOOP", "20.99", "Hula Hoop"),
    LEGO("LEGO", "21.99", "Lego"),
    LINCOLN_LOGS("LINCOLNLOGS", "22.99", "Lincoln Logs"),
    MARBLES("MARBLES", "23.99", "Marbles"),
    MONOPOLY("MONOPOLY", "24.99", "Monopoly");

    private final String name;
    private final String price;
    private final String formalName;
    
    private Toys(String name, String price, String formalName) {
        this.name = name;
        this.price = price;
        this.formalName = formalName;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String getName() {
        return this.formalName;
    }

    public String getPrice() {
        return this.price;
    }

    static public String formatName(String name) {
        return name.replaceAll("\\W", "").toUpperCase();
    }

    static public Toys getToy(String name) throws EnumConstantNotPresentException {
        name = formatName(name);
        for (Toys toy : Toys.values()) {
            if (toy.name.equals(name)) {
                return toy;
            }
        }
        throw new EnumConstantNotPresentException(Toys.class, name);
    }
}
