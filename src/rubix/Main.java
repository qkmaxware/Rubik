/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rubix;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import plus.JSON.JSONobject;
import plus.JSON.JSONparser;

import plus.graphics.*;
import plus.machinelearning.ClassicNetwork;
import plus.machinelearning.NeuralNetwork;
import plus.machinelearning.TrainingData;
import plus.system.Debug;
import plus.system.Resources;
import search.AStar;
import search.BreadthFirstSearch;

/**
 *
 * @author Colin
 */
public class Main {
    
    private static Program p;
    
    private static class TextAreaOutputStream extends OutputStream{
        private JTextArea area;
        
        public TextAreaOutputStream(JTextArea area){
            this.area = area;
        }
        
        public void write(int b) throws IOException{
            area.setCaretPosition(area.getDocument().getLength());
            area.append(String.valueOf((char)b));
        }
    }
    
    private static String defaultParam = "trainer";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        if(args.length == 0)
            args = new String[]{defaultParam};
        
        if(args[0].equals("trainer")){
            StartConsoleApplication();
        }else{
            StartGuiApplication();
        }
    }
    
    private static void StartConsoleApplication(){
        Scanner scanner = new Scanner(System.in);
        boolean run = true;
        System.out.println("Rubix Cube Application - Colin Halseth");
        Program p = new Program();
        
        while(run){
            System.out.println("------------------------------------------------");
            System.out.println("Options { \n\tClose App (quit), \n\tNew NN (nnn), \n\tExport NN (enn), \n\tLoad NN (lnn) ,\n\tTrain NN (tnn) , \n\tTest/Use NN (unn) \n\tList Saved NN (listnn) \n\tList Saved Training Data (listdat)\n}");
            
            String in = scanner.nextLine().trim().toLowerCase();
            
            if(in.equals("quit")){
                run = false;
                break;
            }
            else if(in.equals("nnn")){
                System.out.println("input size: ");
                String i = scanner.nextLine().trim().toLowerCase();
                System.out.println("output size: ");
                String o = scanner.nextLine().trim().toLowerCase();
                System.out.println("hidden layer size: ");
                String h = scanner.nextLine().trim().toLowerCase();
                
                p.CreateNetwork(1, Integer.parseInt(i), Integer.parseInt(o), Integer.parseInt(h));
            }
            else if(in.equals("enn")){
                System.out.println("save name: ");
                String pt = scanner.nextLine().trim();
                p.SaveActiveNetwork(pt);
            }
            else if(in.equals("lnn")){
                System.out.println("path to JSON: ");
                String pt = scanner.nextLine().trim();
                p.LoadNetwork(pt);
            }else if(in.equals("tnn")){
                System.out.println("path to training data: ");
                String pt = scanner.nextLine().trim();
                TrainingData o = p.CreateTrainingData(pt);
                Debug.Log(o);
                p.TrainNetwork(o);
            }
            else if(in.equals("unn")){
                System.out.println("Please enter your input values (comma separated)");
                String pt = scanner.nextLine().trim().toLowerCase();
                String[] dbls = pt.split(",");
                double[] db = new double[dbls.length];
                try{
                    for(int i = 0; i < dbls.length; i++){
                        db[i] = Double.parseDouble(dbls[i]);
                    }
                    p.FeedNetwork(db);
                }catch(Exception ex){
                    Debug.Log(ex);
                }
            }else if(in.equals("listnn")){
                File folder = new File(p.NetworkDir);
                File[] list = folder.listFiles();
                String fils = "FILES: ";
                for(int i = 0; i < list.length; i++){
                    Debug.Log(list[i].getPath());
                }
            }else if(in.equals("listdat")){
                File folder = new File(p.DataDir);
                File[] list = folder.listFiles();
                String fils = "FILES: ";
                for(int i = 0; i < list.length; i++){
                    Debug.Log(list[i].getPath());
                }
            }
            
            try{
                Thread.sleep(4);
            }catch(Exception e){
            
            }
        }
        
        System.exit(0);
    }
    
    private static void StartGuiApplication(){
        //Create the program
        p = new Program();
        
        //Create JAVA windows and actions
        //3D viewport
        JFrame viewport = new JFrame();
        viewport.add(p.GetViewport());
        viewport.setTitle("ViewPort");
        viewport.setSize(640, 480);
        viewport.setLocation(300, 0);
        viewport.setVisible(true);
        viewport.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Log object
        JFrame log = new JFrame();
        JTextArea area = new JTextArea();
        area.setEditable(false);
        log.add(new JScrollPane(area));
        System.setOut(new PrintStream(new TextAreaOutputStream(area)));
        System.setErr(new PrintStream(new TextAreaOutputStream(area)));
        log.setTitle("Log");
        log.setSize(640, 300);
        log.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        log.setLocation(0, 500);
        log.setVisible(true);
        
        Resources.LoadImage("CubeRotateSlice.png");
        Resources.LoadImage("CubeRotateColumn.png");
        Resources.LoadImage("CubeRotateRow.png");
        
        //User controls
        JFrame userCtrl = new JFrame();
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(CreateRotationPanel(Resources.GetImage("CubeRotateRow"), Spin.Mode.Row));
        content.add(CreateRotationPanel(Resources.GetImage("CubeRotateColumn"), Spin.Mode.Column));
        content.add(CreateRotationPanel(Resources.GetImage("CubeRotateSlice"), Spin.Mode.Slice));
        userCtrl.add(content);
        userCtrl.setTitle("Apply Rotation");
        userCtrl.setSize(800, 600);
        userCtrl.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        userCtrl.setLocation(300 + 645, 0);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        
        panel.add(new JLabel("Manual Controls"),gbc);
        
        JButton newCube = new JButton("New Cube");
        newCube.addActionListener((event) -> {
            String dimentions = JOptionPane.showInputDialog("Side Length?", "3");
            int i = Integer.parseInt(dimentions);
            i = Math.max(1, i);
            p.NewCube(i);
        });
        
        JButton perturb = new JButton("Perturb Cube");
        perturb.addActionListener((event) -> {
            String dimentions = JOptionPane.showInputDialog("Number of Perturbations?", "1");
            int i = Integer.parseInt(dimentions);
            p.ApplyRandomSpin(i);
        });
        
        JButton rotate = new JButton("Rotate Faces");
        rotate.addActionListener((event) -> {
            userCtrl.setVisible(true);
        });
        
        JButton solveBF = new JButton("Solve (Breadth First)");
        solveBF.addActionListener((event) -> {
            p.Solve(new BreadthFirstSearch());
        });
        
        JButton solveAS = new JButton("Solve (A*)");
        solveAS.addActionListener((event) -> {
            p.Solve(new AStar());
        });
        
        JButton showLog = new JButton("Show Output Log");
        showLog.addActionListener((event) -> {
            log.setVisible(true);
        });
        
        //Neural network manager
        JFrame networkmgr = new JFrame();
        networkmgr.setTitle("Neural Network Manager");
        networkmgr.setSize(640, 480);
        networkmgr.setLayout(new BorderLayout());
        JLabel allnets = new JLabel("no active network");
        JPanel selectnet = new JPanel();
        JButton nNetwork = new JButton("New Network");
        nNetwork.addActionListener((event)->{
            JFrame n = new JFrame();
            n.setTitle("Spooling Up New Network");
            n.setSize(400, 200);
            
            JPanel pn = new JPanel();
            pn.setLayout(new BoxLayout(pn, BoxLayout.Y_AXIS));
            n.add(pn);
            
            JLabel label = new JLabel("Input Size");
            JTextField inputs = new JTextField("2");
            pn.add(label);
            pn.add(inputs);
            
            label = new JLabel("Output Size");
            JTextField outss = new JTextField("1");
            pn.add(label);
            pn.add(outss);
            
            label = new JLabel("Hidden Layer Sizes (size 1, size 2 ...)");
            JTextField hid = new JTextField("2");
            pn.add(label);
            pn.add(hid);
            
            JButton start = new JButton("Spool Network");
            pn.add(start);
            start.addActionListener((e) -> {
                try{
                    int in = Integer.parseInt(inputs.getText());
                    int out = Integer.parseInt(outss.getText());
                    
                    String[] h = hid.getText().split(",");
                    int[] hidden = new int[h.length];
                    String k = "";
                    for(int i = 0; i < h.length; i++){
                        hidden[i] = Integer.parseInt(h[i]);
                        k += ((i != 0)? "x" : "")+h[i];
                    }
                    
                    NeuralNetwork net = p.CreateNetwork(1, in, out, hidden);
                    allnets.setText(in+"x"+k+"x"+out+ " network");
                    n.setVisible(false);
                    n.dispose();
                }catch(Exception ex){
                    Debug.Log(ex);
                }
            });
            
            n.setVisible(true);
            
        });
        
        JButton loadNetwork = new JButton("Load Network");
        loadNetwork.addActionListener((e) -> {
            JFileChooser fc = new JFileChooser();
            fc.setAcceptAllFileFilterUsed(true);
            fc.addChoosableFileFilter(new FileFilter(){
                @Override
                public boolean accept(File file) {
                    if(file.isDirectory())
                        return false;
                    
                    return true;
                }

                @Override
                public String getDescription() {
                    return "JSON Encoded Neural Network";
                }
            });
            
            int retval = fc.showDialog(null, "Load Network");
            if(retval == JFileChooser.APPROVE_OPTION){
                File file = fc.getSelectedFile();
                p.LoadNetwork(file.getPath());
            }
        });
        
        JButton saveNetwork = new JButton("Save Network");
        saveNetwork.addActionListener((e) ->  {
            p.SaveActiveNetwork("EncodedNetwork");
        });
        
        selectnet.add(allnets);
        selectnet.add(nNetwork);
        selectnet.add(saveNetwork);
        selectnet.add(loadNetwork);
        networkmgr.add(selectnet, BorderLayout.NORTH);
        
        JButton shownetwork = new JButton("Show NN Manager");
        shownetwork.addActionListener((event)->{
            networkmgr.setVisible(true);
            //allnets.setText();
        });
        
        panel.add(newCube,gbc);
        panel.add(perturb,gbc);
        panel.add(rotate,gbc);
        panel.add(solveBF,gbc);
        panel.add(solveAS,gbc);
        panel.add(showLog,gbc);
        panel.add(shownetwork, gbc);
        
        
        panel.add(new JLabel("Programs"),gbc);
        
        JButton dataBF = new JButton("Create Training Data (Breadth First)");
        dataBF.addActionListener((event) -> {
            JOptionPane.showMessageDialog(null, "This experiement will use Breadth First search to solve 1 rubix cube for 1 to 8 random moves. The output is saved to file.");
            //RunProgramOne(new BreadthFirstSearch(), 3, 5, 8);
            p.RunSolveLoop(new BreadthFirstSearch(), 3, 1, 8);
        });
        
        JButton dataAS = new JButton("Create Training Data (A*)");
        dataAS.addActionListener((event) -> {
            JOptionPane.showMessageDialog(null, "This experiement will use A* search to solve 5 rubix cubes. Each cube is pertubed between 1 and 8 times. The output is saved to file.");
            //RunProgramOne(new AStar(), 3, 5, 8);
            p.RunSolveLoop(new AStar(), 3, 5, 8);
        });
        
        JButton tests = new JButton("Run Unit Tests");
        tests.addActionListener((event) -> {
            p.RunUnitTests();
        });
        
        panel.add(dataBF,gbc);
        panel.add(dataAS,gbc);
        panel.add(tests, gbc);
        
        JFrame frame = new JFrame();
        frame.add(panel);
        frame.setTitle("AI PROJECT 2017");
        frame.setSize(300, 500);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        p.Start();
    }
    
    private static JPanel CreateRotationPanel(Bitmap map, Spin.Mode mode){
        JPanel content = new JPanel();
        content.setLayout(new GridLayout(1, 2));
        JLabel label = new JLabel("Rotate "+mode.toString());

        BufferedImage img = map.GetImage();
        JPanel drawingPanel = new JPanel(){
            @Override
            public void paintComponent(Graphics G){
                G.drawImage(img, 0, 0, null);
            }
        };

        JPanel things = new JPanel();
        things.setLayout(new GridLayout(2,3));
        
        JTextField text = new JTextField("0");
        JButton cc = new JButton("Counter Clockwise");
        cc.addActionListener((event) -> {
            //Spin CounterClockwise
            String slice = text.getText();
            try{
                int i = Integer.parseInt(slice);
                i = (i < 0)? 0 : ((i >= p.CubeSize())? p.CubeSize() - 1: i);
                Spin spin = new Spin(i, mode, Spin.Direction.CounterClockwise);
                p.ApplySpin(spin);         
            }catch(Exception e){
                e.printStackTrace();
            }
        });
        
        JButton cw = new JButton("Clockwise");
        cw.addActionListener((event) -> {
            //Spin Clockwise
            String slice = text.getText();
            try{
                int i = Integer.parseInt(slice);
                i = (i < 0)? 0 : ((i >= p.CubeSize())? p.CubeSize() - 1: i);
                Spin spin = new Spin(i, mode, Spin.Direction.Clockwise);
                p.ApplySpin(spin);
            }catch(Exception e){
                e.printStackTrace();
            }
        });
        things.add(label);
        things.add(new JLabel());
        things.add(new JLabel());
        
        things.add(text);
        things.add(cc);
        things.add(cw);
        
        content.add(things);
        content.add(drawingPanel);
        
        return content;
    }
    
}
