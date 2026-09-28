package com.healthfirst.pims;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class PIMSApp {
    static DB db;
    static JFrame frame;
    static final Color TEAL=new Color(0,121,140), NAVY=new Color(24,45,75);

    public static void main(String[] args){
        SwingUtilities.invokeLater(()->{
            try{db=new DB(); login();}catch(SQLException e){
                JOptionPane.showMessageDialog(null,"Unable to connect to MySQL.\nConfigure PIMS_DB_URL, PIMS_DB_USER and PIMS_DB_PASSWORD, then run database.sql.\n\n"+e.getMessage(),"Database error",0);
                System.exit(1);
            }
        });
    }
    static void login(){
        frame=new JFrame("HealthFirst Pharmacy - PIMS"); frame.setDefaultCloseOperation(3); frame.setSize(520,360); frame.setLocationRelativeTo(null);
        JPanel p=new JPanel(new GridLayout(5,1,8,8)); p.setBorder(BorderFactory.createEmptyBorder(25,35,25,35));
        JLabel h=new JLabel("HealthFirst Pharmacy PIMS",SwingConstants.CENTER); h.setFont(new Font("SansSerif",1,22)); h.setForeground(NAVY);
        JTextField u=new JTextField(); JPasswordField pw=new JPasswordField(); JButton b=new JButton("Login"); b.setBackground(TEAL); b.setForeground(Color.WHITE);
        p.add(h);p.add(new JLabel("Username"));p.add(u);p.add(new JLabel("Password"));p.add(pw);p.add(b);
        b.addActionListener(e->{try{User x=db.login(u.getText().trim(),new String(pw.getPassword())); if(x==null) JOptionPane.showMessageDialog(frame,"Invalid username or password"); else if("Admin".equals(x.role)) admin(x); else cashier(x);}catch(Exception ex){err(ex);}});
        frame.setContentPane(p);frame.setVisible(true);
    }
    static void admin(User user){
        JPanel root=new JPanel(new BorderLayout(8,8)); root.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JLabel title=new JLabel("Administrator Dashboard");title.setFont(new Font("SansSerif",1,22));root.add(title,BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane(); tabs.addTab("Medicines",medicines());tabs.addTab("Suppliers",suppliers());tabs.addTab("Users",users());tabs.addTab("Reports",reports());
        root.add(tabs,BorderLayout.CENTER); JButton out=new JButton("Logout");out.addActionListener(e->login());root.add(out,BorderLayout.SOUTH);
        frame.setContentPane(root);frame.setTitle("PIMS - Administrator");frame.setSize(1100,700);frame.setLocationRelativeTo(null);frame.revalidate();
    }
    static JPanel medicines(){
        JPanel p=new JPanel(new BorderLayout(6,6)); String[] c={"ID","Name","Company","Type","Price","Stock","Reorder","Expiry","Supplier"};
        DefaultTableModel m=new DefaultTableModel(c,0); JTable t=new JTable(m); p.add(new JScrollPane(t),BorderLayout.CENTER);
        JPanel f=new JPanel(new GridLayout(2,8,4,4)); JTextField n=new JTextField(),co=new JTextField(),ty=new JTextField("Tablet"),pr=new JTextField(),st=new JTextField(),ro=new JTextField(),ex=new JTextField("2027-12-31"); JComboBox<SupplierChoice> s=new JComboBox<>();
        JButton add=new JButton("Add Medicine"),ref=new JButton("Refresh"); f.add(new JLabel("Name"));f.add(new JLabel("Company"));f.add(new JLabel("Type"));f.add(new JLabel("Price"));f.add(new JLabel("Stock"));f.add(new JLabel("Reorder"));f.add(new JLabel("Expiry"));f.add(new JLabel("Supplier"));f.add(n);f.add(co);f.add(ty);f.add(pr);f.add(st);f.add(ro);f.add(ex);f.add(s);p.add(f,BorderLayout.NORTH);
        JPanel a=new JPanel();a.add(add);a.add(ref);p.add(a,BorderLayout.SOUTH);
        Runnable load=()->{try{m.setRowCount(0);for(Object[] r:db.medicines())m.addRow(r);s.removeAllItems();for(SupplierChoice x:db.suppliers())s.addItem(x);}catch(Exception e){err(e);}};
        load.run();ref.addActionListener(e->load.run());add.addActionListener(e->{try{SupplierChoice x=(SupplierChoice)s.getSelectedItem();db.addMedicine(n.getText(),co.getText(),ty.getText(),Double.parseDouble(pr.getText()),Integer.parseInt(st.getText()),Integer.parseInt(ro.getText()),LocalDate.parse(ex.getText()),x.id);load.run();}catch(Exception z){err(z);}});return p;
    }
    static JPanel suppliers(){
        JPanel p=new JPanel(new BorderLayout(6,6));String[] c={"ID","Name","Contact","Phone","Email","Address"};DefaultTableModel m=new DefaultTableModel(c,0);JTable t=new JTable(m);p.add(new JScrollPane(t));
        JPanel f=new JPanel(new GridLayout(2,6,4,4));JTextField n=new JTextField(),co=new JTextField(),ph=new JTextField(),em=new JTextField(),ad=new JTextField();JButton add=new JButton("Add Supplier"),ref=new JButton("Refresh");
        f.add(new JLabel("Name"));f.add(new JLabel("Contact"));f.add(new JLabel("Phone"));f.add(new JLabel("Email"));f.add(new JLabel("Address"));f.add(new JLabel());f.add(n);f.add(co);f.add(ph);f.add(em);f.add(ad);f.add(add);p.add(f,BorderLayout.NORTH);p.add(ref,BorderLayout.SOUTH);
        Runnable load=()->{try{m.setRowCount(0);for(Object[] r:db.suppliersRows())m.addRow(r);}catch(Exception e){err(e);}};load.run();ref.addActionListener(e->load.run());add.addActionListener(e->{try{db.addSupplier(n.getText(),co.getText(),ph.getText(),em.getText(),ad.getText());load.run();}catch(Exception z){err(z);}});return p;
    }
    static JPanel users(){
        JPanel p=new JPanel(new BorderLayout(6,6));String[] c={"ID","Username","Role","Full Name"};DefaultTableModel m=new DefaultTableModel(c,0);p.add(new JScrollPane(new JTable(m)));
        JPanel f=new JPanel(new GridLayout(2,4,4,4));JTextField u=new JTextField(),name=new JTextField();JPasswordField pw=new JPasswordField();JComboBox<String> role=new JComboBox<>(new String[]{"Admin","Cashier"});JButton add=new JButton("Create User"),ref=new JButton("Refresh");
        f.add(new JLabel("Username"));f.add(new JLabel("Password"));f.add(new JLabel("Role"));f.add(new JLabel("Full Name"));f.add(u);f.add(pw);f.add(role);f.add(name);p.add(f,BorderLayout.NORTH);JPanel a=new JPanel();a.add(add);a.add(ref);p.add(a,BorderLayout.SOUTH);
        Runnable load=()->{try{m.setRowCount(0);for(Object[] r:db.users())m.addRow(r);}catch(Exception e){err(e);}};load.run();ref.addActionListener(e->load.run());add.addActionListener(e->{try{db.addUser(u.getText(),new String(pw.getPassword()),(String)role.getSelectedItem(),name.getText());load.run();}catch(Exception z){err(z);}});return p;
    }
    static JPanel reports(){
        JPanel p=new JPanel(new GridLayout(2,2,8,8));p.add(report("Sales Report",()->db.sales(),new String[]{"Sale ID","Date","Total","Cashier"}));p.add(report("Item-Wise Report",()->db.itemWise(),new String[]{"Medicine","Qty","Revenue"}));p.add(report("Low Stock Report",()->db.lowStock(),new String[]{"Medicine","Stock","Reorder"}));p.add(report("Expiry Report",()->db.expiry(),new String[]{"Medicine","Expiry","Days Left"}));return p;
    }
    interface Rows{java.util.List<Object[]> get() throws Exception;}
    static JPanel report(String title,Rows rows,String[] cols){JPanel p=new JPanel(new BorderLayout());p.add(new JLabel(title),BorderLayout.NORTH);DefaultTableModel m=new DefaultTableModel(cols,0);p.add(new JScrollPane(new JTable(m)));try{for(Object[] r:rows.get())m.addRow(r);}catch(Exception e){err(e);}return p;}
    static void cashier(User user){
        JPanel p=new JPanel(new BorderLayout(8,8));DefaultTableModel m=new DefaultTableModel(new String[]{"Medicine","Price","Stock","Qty"},0);JTable table=new JTable(m);
        JComboBox<MedicineChoice> med=new JComboBox<>();JSpinner qty=new JSpinner(new SpinnerNumberModel(1,1,999,1));JButton add=new JButton("Add to Cart"),checkout=new JButton("Checkout"),refresh=new JButton("Refresh");JTextArea bill=new JTextArea();
        JPanel top=new JPanel();top.add(new JLabel("Medicine"));top.add(med);top.add(new JLabel("Quantity"));top.add(qty);top.add(add);top.add(refresh);p.add(top,BorderLayout.NORTH);p.add(new JScrollPane(table),BorderLayout.CENTER);p.add(new JScrollPane(bill),BorderLayout.EAST);JPanel bot=new JPanel();bot.add(checkout);JButton out=new JButton("Logout");bot.add(out);p.add(bot,BorderLayout.SOUTH);
        java.util.List<Cart> cart=new ArrayList<>();Runnable load=()->{try{med.removeAllItems();for(MedicineChoice x:db.medicineChoices())med.addItem(x);}catch(Exception e){err(e);}};load.run();refresh.addActionListener(e->load.run());
        add.addActionListener(e->{MedicineChoice x=(MedicineChoice)med.getSelectedItem();if(x==null)return;try{int q=(Integer)qty.getValue();double price=db.price(x.id);cart.add(new Cart(x.id,x.name,q,price));m.addRow(new Object[]{x.name,price,db.stock(x.id),q});}catch(Exception z){err(z);}});
        checkout.addActionListener(e->{try{long id=db.checkout(user.id,cart);double total=0;bill.setText("HEALTHFIRST PHARMACY\nSale #"+id+"\n\n");for(Cart c:cart){double v=c.qty*c.price;total+=v;bill.append(c.name+" x"+c.qty+" = "+String.format("%.2f",v)+"\n");}bill.append("\nTOTAL: "+String.format("%.2f",total));cart.clear();m.setRowCount(0);load.run();}catch(Exception z){err(z);}});
        out.addActionListener(e->login());frame.setContentPane(p);frame.setTitle("PIMS - Cashier");frame.setSize(1100,700);frame.setLocationRelativeTo(null);frame.revalidate();
    }
    static void err(Exception e){JOptionPane.showMessageDialog(frame,e.getMessage(),"Error",0);}
    static String hash(String s)throws Exception{byte[] d=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();for(byte x:d)b.append(String.format("%02x",x));return b.toString();}
    record User(int id,String username,String role,String fullName){} record SupplierChoice(int id,String name){public String toString(){return name;}} record MedicineChoice(int id,String name){public String toString(){return name;}} record Cart(int id,String name,int qty,double price){}
    static class DB{
        Connection c;
        DB() throws SQLException {
            try { Class.forName("org.sqlite.JDBC"); } catch (ClassNotFoundException e) { throw new SQLException("SQLite driver unavailable", e); }
            c=DriverManager.getConnection("jdbc:sqlite:pims.db");
            c.setAutoCommit(true);
            init();
        }
        void init() throws SQLException {
            try(Statement s=c.createStatement()){
                s.executeUpdate("PRAGMA foreign_keys=ON");
                s.executeUpdate("CREATE TABLE IF NOT EXISTS users(user_id INTEGER PRIMARY KEY AUTOINCREMENT,username TEXT UNIQUE NOT NULL,password_hash TEXT NOT NULL,role TEXT NOT NULL,full_name TEXT NOT NULL)");
                s.executeUpdate("CREATE TABLE IF NOT EXISTS suppliers(supplier_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,contact_person TEXT,phone TEXT,email TEXT,address TEXT)");
                s.executeUpdate("CREATE TABLE IF NOT EXISTS medicines(medicine_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,company TEXT,medicine_type TEXT,price REAL NOT NULL,quantity_in_stock INTEGER NOT NULL,reorder_level INTEGER NOT NULL,expiry_date TEXT NOT NULL,supplier_id INTEGER,FOREIGN KEY(supplier_id) REFERENCES suppliers(supplier_id) ON UPDATE CASCADE ON DELETE SET NULL)");
                s.executeUpdate("CREATE TABLE IF NOT EXISTS sales(sale_id INTEGER PRIMARY KEY AUTOINCREMENT,sale_date TEXT DEFAULT CURRENT_TIMESTAMP,total_amount REAL NOT NULL,user_id INTEGER NOT NULL,FOREIGN KEY(user_id) REFERENCES users(user_id))");
                s.executeUpdate("CREATE TABLE IF NOT EXISTS sale_items(sale_item_id INTEGER PRIMARY KEY AUTOINCREMENT,sale_id INTEGER NOT NULL,medicine_id INTEGER NOT NULL,quantity_sold INTEGER NOT NULL,price_at_sale REAL NOT NULL,FOREIGN KEY(sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE,FOREIGN KEY(medicine_id) REFERENCES medicines(medicine_id))");
            }
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM users")){r.next();if(r.getInt(1)==0) seed();}
        }
        void seed() throws SQLException {
            try(PreparedStatement s=c.prepareStatement("INSERT INTO users(username,password_hash,role,full_name) VALUES(?,?,?,?)")){
                s.setString(1,"admin");s.setString(2,"240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9");s.setString(3,"Admin");s.setString(4,"System Administrator");s.executeUpdate();
                s.setString(1,"cashier");s.setString(2,"c246650737293ddc18fc357393db78d1ecc9d1fd1af95469115e4a29f983359a");s.setString(3,"Cashier");s.setString(4,"Front Desk Cashier");s.executeUpdate();
            }
            try(PreparedStatement s=c.prepareStatement("INSERT INTO suppliers(name,contact_person,phone,email,address) VALUES(?,?,?,?,?)")){
                Object[][] a={{"MediSupply SA","Thandi Mokoena","011-555-1000","sales@medisupply.co.za","Johannesburg"},{"PharmaLink","David Naidoo","011-555-2000","sales@pharmalink.co.za","Pretoria"}};
                for(Object[] x:a){for(int i=0;i<5;i++)s.setObject(i+1,x[i]);s.executeUpdate();}
            }
            try(PreparedStatement s=c.prepareStatement("INSERT INTO medicines(name,company,medicine_type,price,quantity_in_stock,reorder_level,expiry_date,supplier_id) VALUES(?,?,?,?,?,?,?,?)")){
                Object[][] a={{"Paracetamol 500mg","HealthCo","Tablet",25.99,120,20,"2027-03-20",1},{"Amoxicillin 250mg","PharmaCare","Capsule",49.50,18,25,"2027-01-15",2},{"Cough Syrup 100ml","Wellness Labs","Syrup",68.75,7,10,"2026-10-15",1}};
                for(Object[] x:a){for(int i=0;i<8;i++)s.setObject(i+1,x[i]);s.executeUpdate();}
            }
        }
        User login(String u,String p)throws Exception{try(PreparedStatement s=c.prepareStatement("SELECT user_id,username,role,full_name FROM users WHERE username=? AND password_hash=?")){s.setString(1,u);s.setString(2,hash(p));try(ResultSet r=s.executeQuery()){return r.next()?new User(r.getInt(1),r.getString(2),r.getString(3),r.getString(4)):null;}}}
        java.util.List<SupplierChoice> suppliers()throws SQLException{java.util.List<SupplierChoice>a=new ArrayList<>();try(PreparedStatement s=c.prepareStatement("SELECT supplier_id,name FROM suppliers ORDER BY name");ResultSet r=s.executeQuery()){while(r.next())a.add(new SupplierChoice(r.getInt(1),r.getString(2)));}return a;}
        java.util.List<Object[]> suppliersRows()throws SQLException{return query("SELECT supplier_id,name,contact_person,phone,email,address FROM suppliers ORDER BY supplier_id");}
        java.util.List<Object[]> users()throws SQLException{return query("SELECT user_id,username,role,full_name FROM users ORDER BY user_id");}
        java.util.List<Object[]> medicines()throws SQLException{return query("SELECT m.medicine_id,m.name,m.company,m.medicine_type,m.price,m.quantity_in_stock,m.reorder_level,m.expiry_date,COALESCE(s.name,'-') FROM medicines m LEFT JOIN suppliers s ON s.supplier_id=m.supplier_id ORDER BY m.medicine_id");}
        java.util.List<Object[]> query(String q)throws SQLException{java.util.List<Object[]>a=new ArrayList<>();try(Statement s=c.createStatement();ResultSet r=s.executeQuery(q)){int n=r.getMetaData().getColumnCount();while(r.next()){Object[]x=new Object[n];for(int i=0;i<n;i++)x[i]=r.getObject(i+1);a.add(x);}}return a;}
        void addSupplier(String n,String co,String ph,String em,String ad)throws SQLException{try(PreparedStatement s=c.prepareStatement("INSERT INTO suppliers(name,contact_person,phone,email,address) VALUES(?,?,?,?,?)")){s.setString(1,n);s.setString(2,co);s.setString(3,ph);s.setString(4,em);s.setString(5,ad);s.executeUpdate();}}
        void addUser(String u,String p,String r,String n)throws Exception{try(PreparedStatement s=c.prepareStatement("INSERT INTO users(username,password_hash,role,full_name) VALUES(?,?,?,?)")){s.setString(1,u);s.setString(2,hash(p));s.setString(3,r);s.setString(4,n);s.executeUpdate();}}
        void addMedicine(String n,String co,String ty,double pr,int st,int ro,LocalDate ex,int sid)throws SQLException{try(PreparedStatement s=c.prepareStatement("INSERT INTO medicines(name,company,medicine_type,price,quantity_in_stock, reorder_level,expiry_date,supplier_id) VALUES(?,?,?,?,?,?,?,?)")){s.setString(1,n);s.setString(2,co);s.setString(3,ty);s.setDouble(4,pr);s.setInt(5,st);s.setInt(6,ro);s.setString(7,ex.toString());s.setInt(8,sid);s.executeUpdate();}}
        java.util.List<MedicineChoice> medicineChoices()throws SQLException{java.util.List<MedicineChoice>a=new ArrayList<>();try(PreparedStatement s=c.prepareStatement("SELECT medicine_id,name FROM medicines WHERE quantity_in_stock>0 ORDER BY name");ResultSet r=s.executeQuery()){while(r.next())a.add(new MedicineChoice(r.getInt(1),r.getString(2)));}return a;}
        double price(int id)throws SQLException{try(PreparedStatement s=c.prepareStatement("SELECT price FROM medicines WHERE medicine_id=?")){s.setInt(1,id);try(ResultSet r=s.executeQuery()){if(!r.next())throw new SQLException("Medicine not found");return r.getDouble(1);}}}
        int stock(int id)throws SQLException{try(PreparedStatement s=c.prepareStatement("SELECT quantity_in_stock FROM medicines WHERE medicine_id=?")){s.setInt(1,id);try(ResultSet r=s.executeQuery()){if(!r.next())throw new SQLException("Medicine not found");return r.getInt(1);}}}
        long checkout(int uid,java.util.List<Cart>items)throws SQLException{if(items.isEmpty())throw new SQLException("Cart is empty");c.setAutoCommit(false);try{double total=0;for(Cart x:items){int av=lockStock(x.id);if(x.qty<=0||x.qty>av)throw new SQLException("Insufficient stock for "+x.name);total+=x.qty*x.price;}long sale;try(PreparedStatement s=c.prepareStatement("INSERT INTO sales(total_amount,user_id) VALUES(?,?)",Statement.RETURN_GENERATED_KEYS)){s.setDouble(1,total);s.setInt(2,uid);s.executeUpdate();try(ResultSet r=s.getGeneratedKeys()){r.next();sale=r.getLong(1);}}try(PreparedStatement a=c.prepareStatement("INSERT INTO sale_items(sale_id,medicine_id,quantity_sold,price_at_sale) VALUES(?,?,?,?)");PreparedStatement b=c.prepareStatement("UPDATE medicines SET quantity_in_stock=quantity_in_stock-? WHERE medicine_id=?")){for(Cart x:items){a.setLong(1,sale);a.setInt(2,x.id);a.setInt(3,x.qty);a.setDouble(4,x.price);a.addBatch();b.setInt(1,x.qty);b.setInt(2,x.id);b.addBatch();}a.executeBatch();b.executeBatch();}c.commit();c.setAutoCommit(true);return sale;}catch(Exception e){try{c.rollback();}catch(SQLException ignored){}c.setAutoCommit(true);throw e instanceof SQLException?(SQLException)e:new SQLException(e);}}
        int lockStock(int id)throws SQLException{try(PreparedStatement s=c.prepareStatement("SELECT quantity_in_stock FROM medicines WHERE medicine_id=?")){s.setInt(1,id);try(ResultSet r=s.executeQuery()){if(!r.next())throw new SQLException("Medicine not found");return r.getInt(1);}}}
        java.util.List<Object[]> sales()throws SQLException{return query("SELECT s.sale_id,s.sale_date,s.total_amount,u.username FROM sales s JOIN users u ON u.user_id=s.user_id ORDER BY s.sale_id DESC");}
        java.util.List<Object[]> itemWise()throws SQLException{return query("SELECT m.name,SUM(si.quantity_sold),SUM(si.quantity_sold*si.price_at_sale) FROM sale_items si JOIN medicines m ON m.medicine_id=si.medicine_id GROUP BY m.medicine_id,m.name ORDER BY SUM(si.quantity_sold) DESC");}
        java.util.List<Object[]> lowStock()throws SQLException{return query("SELECT name,quantity_in_stock,reorder_level FROM medicines WHERE quantity_in_stock<=reorder_level ORDER BY quantity_in_stock");}
        java.util.List<Object[]> expiry()throws SQLException{return query("SELECT name,expiry_date,julianday(expiry_date)-julianday('now') FROM medicines ORDER BY expiry_date");}
    }
}
}