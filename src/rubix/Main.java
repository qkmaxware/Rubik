/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rubix;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Start gui on the swing thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              StartGuiApplication();
            }
        });
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
        final TrainingData data = new TrainingData();
        final JFileChooser fc = new JFileChooser();
        networkmgr.setTitle("Neural Network Manager");
        networkmgr.setSize(640, 480);
        networkmgr.setLayout(new BorderLayout());
        
        JTextArea txt = new JTextArea();
        txt.setEditable(false);
        txt.setPreferredSize(new Dimension(320,480));
        JScrollPane left = new JScrollPane(txt);
        networkmgr.add(left, BorderLayout.WEST);
        
        JTextArea txt2 = new JTextArea();
        txt2.setEditable(false);
        txt2.setPreferredSize(new Dimension(300,400));
        JScrollPane right = new JScrollPane(txt2);
        networkmgr.add(right, BorderLayout.EAST);
        
        JPanel options = new JPanel();
        JButton cleard = new JButton("Clear Training Data");
        cleard.addActionListener((evt) -> { 
            data.Clear();
            txt.setText("");
            txt2.setText("");
        });
        JButton loadd = new JButton("Load Training Data");
        loadd.addActionListener((evt) -> {
            int returnVal = fc.showOpenDialog(null);
            
            if(returnVal != JFileChooser.APPROVE_OPTION){
                return;
            }
            
            File selFile = fc.getSelectedFile();
            try{
            for(String line : Files.readAllLines(Paths.get(selFile.getPath()))){
                String[] lines = line.split("\\|");
                if(data.Add(String2Array(lines[0]), String2Array(lines[1]))){
                    txt.append(lines[0]+"\n");
                    txt2.append(lines[1]+"\n");
                }
            }}catch(Exception e){
                Debug.Log(e);
            };
        });
        JButton startd = new JButton("Train Network");
        startd.addActionListener((evt) -> {
            p.TrainNetwork(data);
        });
   
        JButton saveNetwork = new JButton("Save Network");
        saveNetwork.addActionListener((evt)->{
            p.SaveActiveNetwork("RubixNetwork");
        });
        
        options.add(cleard);
        options.add(loadd);
        options.add(startd);
        options.add(saveNetwork);
        networkmgr.add(options, BorderLayout.SOUTH);
        
        JButton shownetwork = new JButton("Show NN Manager");
        shownetwork.addActionListener((event)->{
            networkmgr.setVisible(true);
        });
        
        panel.add(newCube,gbc);
        panel.add(perturb,gbc);
        panel.add(rotate,gbc);
        panel.add(solveBF,gbc);
        panel.add(solveAS,gbc);
        panel.add(showLog,gbc);
        panel.add(shownetwork, gbc);
        
        
        panel.add(new JLabel("Programs"),gbc);
  
        JButton dataAS = new JButton("Create Training Data (A*)");
        dataAS.addActionListener((event) -> {
            JOptionPane.showMessageDialog(null, "Use A* search to solve many rubix cubes with different numbers of perturbations. The output is saved to file.");
            String pert = JOptionPane.showInputDialog("Max perturbation depth", "3");
            int pv = Integer.parseInt(pert);
            String its = JOptionPane.showInputDialog("Number of tries at each depth", "10");
            int it = Integer.parseInt(its);
            //RunProgramOne(new AStar(), 3, 5, 8);
            p.RunSolveLoop(new AStar(), 3, it, pv);
        });
        
        JButton tests = new JButton("Run Unit Tests");
        tests.addActionListener((event) -> {
            p.RunUnitTests();
        });
        
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
    
    private static double[] String2Array(String values){
        String[] split = values.split("\\,");
        double[] ds = new double[split.length];
        for(int i = 0; i < split.length; i++){
            String s = split[i];
            double d = 0;
            try{
                d = Double.parseDouble(s);
            }catch(Exception e){}
            ds[i] = d;
        }
        return ds;
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
