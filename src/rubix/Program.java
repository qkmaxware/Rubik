/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rubix;

import com.sun.glass.events.KeyEvent;
import encoder.FileWriter;
import encoder.OnesEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import plus.JSON.JSONobject;
import plus.JSON.JSONparser;
import plus.async.AsyncPool;
import plus.game.Game;
import plus.game.GameObject;
import plus.game.GameScene;
import plus.graphics.Bitmap;
import plus.graphics.Camera;
import plus.graphics.Geometry;
import plus.graphics.RenderPanel;
import plus.graphics.Transform;
import plus.graphics.gui.Ui;
import plus.graphics.gui.UiText;
import plus.machinelearning.MatrixNetwork;
import plus.machinelearning.*;
import plus.math.Mathx;
import plus.math.Vector3;
import plus.system.Debug;
import plus.system.ObjParser;
import plus.system.Time;
import search.AStar;
import search.ISearch;
import search.ISearchable;

/**
 *
 * @author Colin
 */
public class Program {
    
    private Cube cube;
    private Transform root;
    
    private Bitmap cubeMap;
    private Bitmap black;
    private Bitmap red;
    private Bitmap blue;
    private Bitmap green;
    private Bitmap yellow;
    private Bitmap white;
    private Bitmap purple;
    
    private Geometry sidePrefab;
    private Geometry topPrefab;
    
    private ConcurrentLinkedQueue<Integer> newCubeRequests = new ConcurrentLinkedQueue<Integer>(); //Deals with my occasional concurrent modification exception by doing it inside the game loop
    private final AsyncPool jobPool = new AsyncPool(2);
    
    private MatrixNetwork network;
    
    private Game game;
    private GameScene scene;
    private RenderPanel view;
    
    private CubeFace topFace;
    private CubeFace bottomFace;
    private CubeFace leftFace;
    private CubeFace rightFace;
    private CubeFace frontFace;
    private CubeFace backFace;
    
    public String DataDir = "Data/Training";
    public String NetworkDir = "Data/Networks";
    
    private static class CubeFace{
        public GameObject parent;
        public Geometry[][] coloredFaces;
        public LinkedList<GameObject> obs = new LinkedList<GameObject>();
    }
    
    public Program(){
        //Initialize base cube
        cube = new Cube(3);
        
        //Initialize BaseLevelBitmap
        cubeMap = new Bitmap(16,16);
        for(int i = 0; i < 16; i++){
            for(int j = 0; j  < 16; j++){
                java.awt.Color color = (i <= 1 || j <= 1 || i >= 14 || j >= 15)? java.awt.Color.black : java.awt.Color.white;
                cubeMap.SetColor(i, j, color);
            }
        }
        
        this.white = new Bitmap(2,2);
        white.SetColor(0, 0, java.awt.Color.white);
        white.SetColor(0, 1, java.awt.Color.white);
        white.SetColor(1, 0, java.awt.Color.white);
        white.SetColor(1, 1, java.awt.Color.white);
        
        this.blue = this.ReplaceColor(this.white,java.awt.Color.BLUE);
        this.green = this.ReplaceColor(this.white,java.awt.Color.green);
        this.yellow = this.ReplaceColor(this.white,java.awt.Color.yellow);
        this.purple = this.ReplaceColor(this.white,new java.awt.Color(144,20,158));
        this.red = this.ReplaceColor(this.white,java.awt.Color.red);
        this.black = this.ReplaceColor(this.white,java.awt.Color.DARK_GRAY);
        
        //Create arrow "pointer" geometry
        Geometry arrow = ObjParser.Parse(
                ("# Blender v2.75 (sub 0) OBJ File: ''\n" +
                "# www.blender.org\n" +
                "mtllib Arrow.mtl\n" +
                "o Plane\n" +
                "v 0.299953 0.000000 -0.030203\n" +
                "v 0.795170 0.000000 -0.030203\n" +
                "v 0.000784 0.000000 -1.000000\n" +
                "v 0.299953 0.000000 1.000000\n" +
                "v -0.298386 0.000000 1.000000\n" +
                "v -0.793603 0.000000 -0.030203\n" +
                "v -0.298386 0.000000 -0.030203\n" +
                "vn 0.000000 -1.000000 0.000000\n" +
                "vn 0.000000 1.000000 0.000000\n" +
                "usemtl None\n" +
                "s off\n" +
                "f 3//1 2//1 1//1\n" +
                "f 6//2 7//2 3//2\n" +
                "f 5//1 7//1 4//1\n" +
                "f 1//1 4//1 7//1\n" +
                "f 7//1 3//1 1//1").split("\n")).get(0).GetRight();
        Bitmap plain = new Bitmap(2,2);
        for(int i = 0; i < plain.GetWidth(); i++){
            plain.SetColor(i, 0, java.awt.Color.WHITE);
            plain.SetColor(i, 1, java.awt.Color.WHITE);
        }
        arrow.SetBitmap(plain);
        
        this.sidePrefab = ObjParser.Parse((
            "# Blender v2.72 (sub 0) OBJ File: ''\n" +
            "# www.blender.org\n" +
            "mtllib rubix_sides.mtl\n" +
            "o Plane.001\n" +
            "v 1.000000 0.000000 -1.000000\n" +
            "v -1.000000 0.000000 -1.000000\n" +
            "v -0.900000 0.100000 -0.900000\n" +
            "v 0.900000 0.100000 -0.900000\n" +
            "v 1.000000 -0.000000 1.000000\n" +
            "v 0.900000 0.100000 0.900000\n" +
            "v -1.000000 -0.000000 1.000000\n" +
            "v -0.900000 0.100000 0.900000\n" +
            "usemtl None\n" +
            "s off\n" +
            "f 2 3 4\n" +
            "f 1 4 6\n"+
            "f 5 6 8\n" +
            "f 7 8 3\n"+
            "f 1 2 4\n" +
            "f 5 1 6\n"+
            "f 7 5 8\n"+
            "f 2 7 3").split("\n")
        ).get(0).GetRight();
        
        this.topPrefab = ObjParser.Parse((
            "# Blender v2.72 (sub 0) OBJ File: ''\n" +
            "# www.blender.org\n" +
            "mtllib rubix_top.mtl\n" +
            "o Plane\n" +
            "v -0.900000 0.100000 0.900000\n" +
            "v 0.900000 0.100000 0.900000\n" +
            "v -0.900000 0.100000 -0.900000\n" +
            "v 0.900000 0.100000 -0.900000\n" +
            "usemtl None\n" +
            "s off\n" +
            "f 1 2 3\n"+
            "f 2 3 4").split("\n")
        ).get(0).GetRight();
        
        //Initialize rendering engine
        game = new Game();                                        //Create a new "Game" aka renderer
        this.view = game.GetViewport();
        scene = new GameScene("main scene");                      //Create a new rendering scene
        game.GetSceneManager().LoadScene(scene);                  //Set this scene as the active scene
        Camera cam = new Camera(640,480);                         //Create a camera to render to
        cam.SetLocalPosition(new Vector3(0,0,-8));                //Set camera's position
        cam.SetRenderMode(Camera.RenderMode.Perspective);         //Use perspective rendering
        scene.camera = cam;                                       //Sets the scene's active camera
        
        GameObject RubixCubeObject = new GameObject("Rubix");     //Create object to act as the cube's origin
        RubixCubeObject.OnUpdate((go, deltatime) -> {             //Create an update function to spin the cube
            Transform t = go.GetTransform();
            
            Vector3 rotation = new Vector3();
            float speed = 150;
            //Rotate the cube
            if(game.input.KeyPressed(KeyEvent.VK_D)){
                rotation = rotation.add(new Vector3(0,-speed,0).scale(deltatime));
            }
            if(game.input.KeyPressed(KeyEvent.VK_A)){
                rotation = rotation.add(new Vector3(0,speed,0).scale(deltatime));
            }
            if(game.input.KeyPressed(KeyEvent.VK_W)){
                rotation = rotation.add(new Vector3(speed,0,0).scale(deltatime));
            }
            if(game.input.KeyPressed(KeyEvent.VK_S)){
                rotation = rotation.add(new Vector3(-speed,0,0).scale(deltatime));
            }
            
            Vector3 newEuler = t.GetEulerAngles().add(rotation);
            Vector3 clampedEuler = new Vector3(Mathx.Clamp(newEuler.x(), -50, 50), newEuler.y(), newEuler.z());
            t.SetEulerAngles(clampedEuler);
            
            //Handle new Cube requests -- concurrent queue to prevent concurrent accesses
            Integer i = newCubeRequests.poll();
            if(i != null){
                this.cube = new Cube(i);
                this.ClearOldModel();
                this.CreateCubeModel(root, cube);
                this.ApplyAllColors();
            }
        });   
        root = RubixCubeObject.transform;
        RubixCubeObject.transform.SetLocalEulerAngles(new Vector3(25, -45, 0));
        scene.Instanciate(RubixCubeObject);
        
        GameObject arrowObj = new GameObject("arrow", arrow);
        arrowObj.GetTransform().SetParent(RubixCubeObject.GetTransform());
        arrowObj.GetTransform().SetLocalPosition(new Vector3(0,0, -1.5));
        arrowObj.GetTransform().SetLocalEulerAngles(new Vector3(0,180,0));
        arrowObj.GetTransform().SetLocalScale(new Vector3(0.3f, 0.3f, 0.3f));
        final Property<Double> timeCap = new Property<Double>(0.0);
        arrowObj.OnUpdate((go, deltatime) -> {
            //Wrap between 0 and 2pi
            float speed = 4;
            timeCap.set(timeCap.get() + speed * deltatime);
            while(timeCap.get() > 2* Math.PI){
                timeCap.set(timeCap.get() - 2 * Math.PI);
            }
            Vector3 offset = new Vector3(0,0, 0.2f * Math.sin(timeCap.get()));
            go.GetTransform().SetLocalPosition(new Vector3(0,0,-1.5).add(offset));
        });
        scene.Instanciate(arrowObj);
        
        GameObject test = new GameObject("test");
        scene.Instanciate(test);
        test.transform.SetParent(RubixCubeObject.GetTransform());
        
        CreateCubeModel(test.transform, this.cube);
        ApplyAllColors();
        
        UiText text = new UiText("<W,A,S,D> Rotates cube when window in focus", java.awt.Color.ORANGE);
        text.anchor = Ui.BottomLeft;
        text.origin = Ui.BottomLeft;
        scene.Instanciate(text);
        
        //Initialize the NN, 324 - 200 - 9 network should be fine... I hope
        int inputs = 324;
        int outputs = 9;
        int[] layers = new int[]{200}; //158
        NetworkTopology topo = plus.machinelearning.NetworkTopology.Construct(inputs, outputs, layers);
        this.network = new MatrixNetwork(topo, ActivationFunction.tanh);
    }
    
    public void Start(){
        this.game.Start();
    }
    
    private void ClearOldModel(){
        ClearFace(this.frontFace);
        ClearFace(this.leftFace);
        ClearFace(this.backFace);
        ClearFace(this.rightFace);
        ClearFace(this.topFace);
        ClearFace(this.bottomFace);
    }
    
    private void ClearFace(CubeFace face){
        if(face == null)
            return;
        
        for(GameObject go : face.obs){
            scene.Destroy(go);
        }
    }
    
    private void ApplyAllColors(){
        ApplyColors(this.cube, Face.Top, this.topFace);
        ApplyColors(this.cube, Face.Bottom, this.bottomFace);
        ApplyColors(this.cube, Face.Left, this.leftFace);
        ApplyColors(this.cube, Face.Right, this.rightFace);
        ApplyColors(this.cube, Face.Front, this.frontFace);
        ApplyColors(this.cube, Face.Back, this.backFace);
    }
    
    private void ApplyColors(Cube c, Face f, CubeFace face){
        for(int i = 0 ; i < c.Length(); i++){
            for(int j = 0; j < c.Length(); j++){
                Bitmap col;
                Color cc = c.Get(f, i, j);
                switch(cc){
                    case Red:
                        col = this.red;
                        break;
                    case White:
                        col = this.white;
                        break;
                    case Blue:
                        col = this.blue;
                        break;
                    case Green:
                        col = this.green;
                        break;
                    case Yellow:
                        col = this.yellow;
                        break;
                    case Purple:
                        col = this.purple;
                        break;
                    default:
                        col = this.black;
                        break;
                }
                face.coloredFaces[i][j].SetBitmap(col);
            }
        }
    }
    
    private void CreateCubeModel(Transform parent, Cube c){
        double scalar = 1.0 / ((c.Length()));
        
        CubeFace top = CreateCubeFace(scalar, Face.Top,c);
        top.parent.transform.SetParent(parent);
        top.parent.transform.SetLocalPosition(top.parent.transform.GetLocalPosition().add(new Vector3(0,-1,0)));
        top.parent.transform.SetLocalEulerAngles(new Vector3(-180,0,0));
        this.topFace = top;
        
        CubeFace bottom = CreateCubeFace(scalar,Face.Bottom, c);
        bottom.parent.transform.SetParent(parent);
        bottom.parent.transform.SetLocalPosition(bottom.parent.transform.GetLocalPosition().add(new Vector3(0,1,0)));
        bottom.parent.transform.SetLocalEulerAngles(new Vector3(0,0,0));
        this.bottomFace = bottom;
        
        CubeFace left = CreateCubeFace(scalar,Face.Left,c);
        left.parent.transform.SetParent(parent);
        left.parent.transform.SetLocalPosition(left.parent.transform.GetLocalPosition().add(new Vector3(-1,0,0)));
        left.parent.transform.SetLocalEulerAngles(new Vector3(90,-90,0));
        left.parent.transform.SetLocalScale(new Vector3(1,-1,-1));
        this.leftFace = left;
        
        CubeFace right = CreateCubeFace(scalar, Face.Right,c);
        right.parent.transform.SetParent(parent);
        right.parent.transform.SetLocalPosition(right.parent.transform.GetLocalPosition().add(new Vector3(1,0,0)));
        right.parent.transform.SetLocalEulerAngles(new Vector3(90,90,0));
        right.parent.transform.SetLocalScale(new Vector3(1,-1,-1));
        this.rightFace = right;
        
        CubeFace front = CreateCubeFace(scalar,Face.Front, c);
        front.parent.transform.SetParent(parent);
        front.parent.transform.SetLocalEulerAngles(new Vector3(-90,0,0));
        front.parent.transform.SetLocalPosition(front.parent.transform.GetLocalPosition().add(new Vector3(0,0,-1)));
        this.frontFace = front;
        
        CubeFace back = CreateCubeFace(scalar, Face.Back, c);
        back.parent.transform.SetParent(parent);
        back.parent.transform.SetLocalEulerAngles(new Vector3(90,0,0));
        back.parent.transform.SetLocalPosition(back.parent.transform.GetLocalPosition().add(new Vector3(0,0,1)));
        back.parent.transform.SetLocalScale(new Vector3(1,-1,1));
        this.backFace = back;
    }
    
    private CubeFace CreateCubeFace(double scalar, Face facet, Cube c){
        CubeFace face = new CubeFace();
        face.coloredFaces = new Geometry[c.Length()][c.Height()];

        //Create top face
        GameObject topFace = new GameObject("top-face");
        Vector3 center = new Vector3(1,0,1);
        for(int i = 0; i < c.Length(); i++){
            for(int j = 0 ; j < c.Height(); j++){
                GameObject tile = new GameObject("tile");
                Geometry t = new Geometry(this.topPrefab);
                t.SetBitmap(this.red);
                GameObject top = new GameObject("color",t);
                Geometry s = new Geometry(this.sidePrefab);
                s.SetBitmap(this.black);
                GameObject side = new GameObject("edge",s);
                
                
                top.transform.SetParent(tile.transform);
                side.transform.SetParent(tile.transform);
                tile.transform.SetParent(topFace.transform);
                tile.transform.SetLocalScale(Vector3.one.scale(scalar));
                tile.transform.SetLocalPosition(new Vector3(scalar + (i*2) * scalar, 0, scalar + (j*2) * scalar).sub(center));
                face.coloredFaces[i][j] = t;
                face.obs.add(side); face.obs.add(top);
                
                this.scene.Instanciate(top);
                this.scene.Instanciate(side);
            }
        }
        
        face.parent = topFace;
        
        return face;
    }
    
    private Bitmap ReplaceColor(Bitmap cubeMap, java.awt.Color newcolor){
        Bitmap m = new Bitmap(cubeMap);
        for(int i = 0; i < m.GetWidth(); i++){
            for(int j = 0; j < m.GetHeight(); j++){
                java.awt.Color color = m.GetColor(i, j);
                if(color.equals(java.awt.Color.white)){
                    m.SetColor(i, j, newcolor);
                }
            }
        }
        return m;
    }

    public void NewCube(int size){
        newCubeRequests.add(size);
        //RegenTextures();
    }
    
    public void ApplySpin(Spin spin){
        cube.Perturb(spin);
        this.ApplyAllColors();
        //RegenTextures();
    }
    
    public void ApplyRandomSpin(int number){
        cube.Perturb(number);
        this.ApplyAllColors();
        //RegenTextures();
    }
    
    public void Solve(ISearch search){
        Debug.Log("Starting search ... Please Wait");
            Property<rubix.Cube> toSolve = new Property(new rubix.Cube(cube));
            jobPool.Enqueue(() -> {
                Time t = new Time();
                LinkedList<ISearchable> path = search.FindPath(toSolve.get(), new rubix.Cube(toSolve.get().Length()));
                Debug.Log("Search results in: "+path.size()+" step(s) after "+t.DeltaTime()+"s");
                Debug.Log("--- Apply the steps below to solve");
                int i = 0;
                for(ISearchable node : path){
                    i++;
                    rubix.Cube step = (rubix.Cube)node;
                    Spin required = step.LastSpin();
                    Debug.Log("--- --- Spin #"+i+" - "+required.toString());
                }
            });
    }
    
    public void RunSolveLoop(ISearch search, int cubeSize, int iterations, int maxPerturbations){
        Debug.Log("Starting Program: Data Generation... please wait this may take some time");
        
        this.jobPool.Enqueue(()->{
            final rubix.Cube solved = new rubix.Cube(cubeSize);
            final OnesEncoder encoder = new OnesEncoder();
            for(int p = 1; p <= maxPerturbations; p++){
                Debug.Log("--- Solving for "+p+" perturbations");
                for(int i = 0; i < iterations; i++){
                    Debug.Log("--- --- Starting iteration "+i);
                    rubix.Cube cube = new rubix.Cube(cubeSize);
                    cube.Perturb(p);

                    LinkedList<ISearchable> results = search.FindPath(cube, solved);
                    results.addFirst(cube);

                    FileWriter writer = new FileWriter(this.DataDir+"/"+search.getClass().getSimpleName()+ "(d"+p+"-#"+i+") on "+cubeSize+"x"+cubeSize,".csv");
                    for(int k = 0; k < results.size()-1; k++){
                        rubix.Cube state = (rubix.Cube)results.get(k);
                        Spin move = ((rubix.Cube)results.get(k+1)).LastSpin();
                        //Encode and output to file size x size -> rng perterbations #2
                        String state_encoding = encoder.Encode(state, 6);   // 3x3 * 6 * 6 = 324
                        String move_encoding = encoder.Encode(move, solved.Length()); //3 values * 3 sized = 9
                        String encoding = state_encoding+"|"+move_encoding;
                        writer.WriteLn(encoding);
                    }  
                    writer.Save();

                }
            }
        });
        /*
        for(int i = 0; i < iterations; i++){
            Property<Integer> iterationId = new Property<Integer>(i);
            plus.system.functional.Action job = () -> {
                rubix.Cube solved = new rubix.Cube(cubeSize);
                
                    Debug.Log("Starting iteration " + (iterationId.get()+1)+"/"+(iterations));
                    for(int p = 1; p <= maxPerturbations; p++){
                        Debug.Log("--- Solving for "+p+" perturbations");
                        //Solve cube of size "cubeSize" with p random perturbations
                        rubix.Cube cube = new rubix.Cube(cubeSize);
                        cube.Perturb(p);
                        LinkedList<ISearchable> results = search.FindPath(cube, solved);
                        Debug.Log("--- --- Solved in "+results.size()+" moves");
                        results.addFirst(cube);
                        FileWriter writer = new FileWriter(this.DataDir+"/"+search.getClass().getSimpleName()+ "(#"+iterationId.get()+"-"+p+") on "+cubeSize+"x"+cubeSize+".csv");
                        for(int k = 0; k < results.size()-1; k++){
                            rubix.Cube state = (rubix.Cube)results.get(k);
                            Spin move = ((rubix.Cube)results.get(k+1)).LastSpin();
                            //Encode and output to file size x size -> rng perterbations #2
                            String state_encoding = encoder.Encode(state, 6);
                            String move_encoding = encoder.Encode(move, solved.Length());
                            String encoding = state_encoding+"|"+move_encoding;
                            writer.WriteLn(encoding);
                        }  
                        writer.Save();
                    }

                Debug.Log("Program Finished");
            };
            jobPool.Enqueue(job);
        }*/
    }
    
    public void LoadNetwork(String path){
        try{
            List<String> lines = Files.readAllLines(Paths.get(path));
            String i = String.join("\n", lines);
            //ClassicNetwork network = ClassicNetwork.FromJSON((JSONobject)(new JSONparser()).Parse(i)); 
            this.network = network;
        }catch(Exception ex){
            Debug.Log(ex);
        }
    }
    
    public void FeedNetwork(double[] ins){
        if(this.network == null)
            return;
        
        double[] out = this.network.Feed(ins).GetData();
        
        Debug.Log(Arrays.toString(out));
        
    }
    
    public void TrainNetwork(TrainingData data){
        if(data == null)
            return;
        
        double[][] input = data.GetInputs();
        double[][] output = data.GetOutputs();
        
        int epochs = 200;
        int iterations = 200;
        double accuracy = 0.1;
        double learningRate = 0.1;
        
        this.jobPool.Enqueue(() -> {
            Debug.Log("Starting Training");
            boolean trained = SBPtrainer.Train(
                network, 
                input, 
                output, 
                epochs, 
                iterations, 
                accuracy, 
                learningRate
            );
            Debug.Log("Training Complete (accurate: "+trained+")");
        });
    }
    
    public void SaveActiveNetwork(String name){
        if(this.network == null)
            return;
        
        FileWriter writer = new FileWriter(this.NetworkDir+"/"+name, ".json");
        writer.WriteLn(this.network.ToJSON());
        writer.Save();
        Debug.Log("Network saved to: "+this.NetworkDir+"/"+name+".json");
    }
    
    public NeuralNetwork GetActiveNetwork(){
        //return this.network;
        return null;
    }
    
    public RenderPanel GetViewport(){
        return this.view;
    }
    
    public int CubeSize(){
        return this.cube.Length();
    }
    
    public void RunUnitTests(){
        jobPool.Enqueue(() -> {
            Debug.Log("Starting Program: Unit Tests");

            AStar search = new AStar();
            
            Debug.Log("--- Testing Size (2) Cube");
            Cube t = new Cube(2);
            Cube t2 = new Cube(2);
            //If spins undo each other, spins are valid
            for(int i = 0; i < 2; i++){
                Spin s1 = new Spin(i, Spin.Mode.Column, Spin.Direction.Clockwise);
                Spin s2 = new Spin(i, Spin.Mode.Column, Spin.Direction.CounterClockwise);
                t2.Perturb(s1); t2.Perturb(s2);
                Debug.Log("--- --- Column Spin Test: "+t2.Equivalent(t));
                
                Spin s3 = new Spin(i, Spin.Mode.Row, Spin.Direction.Clockwise);
                Spin s4 = new Spin(i, Spin.Mode.Row, Spin.Direction.CounterClockwise);
                t2.Perturb(s3); t2.Perturb(s4);
                Debug.Log("--- --- Row Spin Test: "+t2.Equivalent(t));
                
                Spin s5 = new Spin(i, Spin.Mode.Slice, Spin.Direction.Clockwise);
                Spin s6 = new Spin(i, Spin.Mode.Slice, Spin.Direction.CounterClockwise);
                t2.Perturb(s5); t2.Perturb(s6);
                Debug.Log("--- --- Slice Spin Test: "+t2.Equivalent(t));
            }

            t2.Perturb(3);
            //Test if I find a solution at the same depth of perturbations
            Debug.Log("--- --- AStar (depth 3) Found Solution: "+(search.FindPath(t2, t).size() <= 3)); 
            
            Debug.Log("--- Testing Size (3) Cube");

            t = new Cube(3);
            t2 = new Cube(3);
            for(int i = 0; i < 3; i++){
                Spin s1 = new Spin(i, Spin.Mode.Column, Spin.Direction.Clockwise);
                Spin s2 = new Spin(i, Spin.Mode.Column, Spin.Direction.CounterClockwise);
                t2.Perturb(s1); t2.Perturb(s2);
                Debug.Log("--- --- Column Spin Test: "+t2.Equivalent(t));
                
                Spin s3 = new Spin(i, Spin.Mode.Row, Spin.Direction.Clockwise);
                Spin s4 = new Spin(i, Spin.Mode.Row, Spin.Direction.CounterClockwise);
                t2.Perturb(s3); t2.Perturb(s4);
                Debug.Log("--- --- Row Spin Test: "+t2.Equivalent(t));
                
                Spin s5 = new Spin(i, Spin.Mode.Slice, Spin.Direction.Clockwise);
                Spin s6 = new Spin(i, Spin.Mode.Slice, Spin.Direction.CounterClockwise);
                t2.Perturb(s5); t2.Perturb(s6);
                Debug.Log("--- --- Slice Spin Test: "+t2.Equivalent(t));
            }
            t2.Perturb(3);
            //Test if I find a solution at the same depth of perturbations
            Debug.Log("--- --- AStar (depth 3) Found Solution: "+(search.FindPath(t2, t).size() <= 3));
            
            Debug.Log("Program Finished");
        });
    }
    
}
