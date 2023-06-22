import React from 'react'

function LoadingSpinner(props) {
    return <div className="lds-ring" style={props.style} id={props.id}><div></div><div></div><div></div><div></div></div>
}

export default LoadingSpinner