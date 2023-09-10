import logo from './logo.svg';
import './App.css';
import axios from "axios";
import {useEffect, useState} from "react";

function App() {
  const [hello, setHello] = useState('')

  useEffect(() => {
    axios.get('/hello')
        .then(response => setHello(response.data))
        .catch(error => console.log(error))
  }, []);

  return (
    <div className="App">
      백엔드에서 가져온 데이터 =>  {hello}
    </div>
  );
}

export default App;
